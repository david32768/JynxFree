package com.github.david32768.jynxfree.transform;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.david32768.jynxfree.my.Message.M632;
import static com.github.david32768.jynxfree.my.Message.M644;

import com.github.david32768.jynxfree.classfile.CodeBuilderUtility;
import com.github.david32768.jynxfree.classfile.Instructions;
import com.github.david32768.jynxfree.classfile.Opcodes;
import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;
import com.github.david32768.jynxfree.jynx.LogIllegalStateException;

public class CodeArrayBuilder {

    private static final int MAX_OFFSET = 65535;

    private record Offset(int min, int max) {

        private static final int MAX_IF_OFFSET = Short.MAX_VALUE;
        private static final int MIN_IF_OFFSET = Short.MIN_VALUE;
    
        public Offset {
            assert min >= 0;
            assert max >= min;
        }

        public Offset adjust(Instruction inst) {
            int sz = Instructions.sizeOfAt(inst, min);
            int adj = Instructions.largeBranchAdjustment(inst, min);
            return new Offset(min + sz, max + sz + adj);
        }
        
        public Offset adjustDown(BranchInstruction inst) {
            int sz = Instructions.sizeOfAt(inst, min);
            int adj = Instructions.largeBranchAdjustment(inst, min);
            return new Offset(min - sz, max - sz - adj);
        }
        
        public int over() {
            return max - min;
        }
        
        public boolean small(int actual, int over) {
            assert actual >= 0 && actual <= MAX_OFFSET;
            if (min == max || over == 0) {
                return actual + MIN_IF_OFFSET <= min
                    && actual + MAX_IF_OFFSET >= max;                
            }
            assert max - min >= over;
            return actual + MAX_IF_OFFSET >= max - over;
        }
        
        public Offset actual(int actual) {
            if (actual < min || actual > max) {
                // "actual offset %d is not in range [%d,%d]"
                throw new LogIllegalStateException(M632, actual, min , max);
            }
            return new Offset(actual, actual);
        }
        
    }
    
    private final List<CodeElement> code;
    private final Map<Label,Offset> offsetMap;

    private Offset offsets;
    private int wideBranchState;
    private Label iflab;

    private CodeArrayBuilder() {
        this.code = new ArrayList<>();
        this.offsetMap = new HashMap<>();
        this.offsets = new Offset(0,0);
        this.wideBranchState = 0;
        this.iflab = null;
    }

    public static CodeArrayBuilder of() {
        return new CodeArrayBuilder();
    }

    public CodeArrayBuilder opcode(Opcode opcode) {
        return add(CodeBuilderUtility.instructionOf(opcode));
    }
    
    public CodeArrayBuilder opcode(Opcode opcode, int value) {
        return add(CodeBuilderUtility.instructionOf(opcode, value));
    }
    
    public CodeArrayBuilder opcode(Opcode opcode, Label target) {
        return add(CodeBuilderUtility.instructionOf(opcode, target));
    }
    
    public CodeArrayBuilder add(CodeElement element) {
        // non-wide forms of jsr and goto
        element = switch(element) {
            case BranchInstruction br when br.opcode() == Opcode.GOTO_W ->
                BranchInstruction.of(Opcode.GOTO, br.target());
            case DiscontinuedInstruction.JsrInstruction jsr when jsr.opcode() == Opcode.JSR_W ->
                DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR, jsr.target());
            default -> element;  
        };
     
        // change (if lab1, goto lab2, lab1:) -> (if lab2, lab1:)
        if (element instanceof Label label && wideBranchState == 2 && label == iflab) {
            var brgo = removeLast();
            var brif = removeLast();
            var opcode = Opcodes.oppositeBranch(brif.opcode());
            brif = BranchInstruction.of(opcode, brgo.target());
            append(brif);
        }
        
        wideBranchState = switch(element) {
            case BranchInstruction br -> {
                if (br.opcode() == Opcode.GOTO) {
                    yield wideBranchState == 1? 2: 0;
                } else {
                    iflab = br.target();
                    yield 1;
                }
            }
            default -> 0;
        };

        return append(element);
    }

    private CodeArrayBuilder append(CodeElement element) {
        switch (element) {
            case Label label -> offsetMap.put(label, offsets);
            case Instruction inst -> offsets = offsets.adjust(inst);
            default -> {}
        }

        code.add(element);
        return this;
    }
    
    private BranchInstruction removeLast() {
        var br = (BranchInstruction)code.removeLast();
        offsets = offsets.adjustDown(br);
        return br;
    }
    
    public int minSize() {
        return offsets.min;
    }
    
    public int maxSize() {
        return offsets.max;
    }
    
    public void atEnd(CodeBuilder builder) {
        int offset = 0;
        int over = 0;
        for (var element : code) {
            switch (element) {
                case Label label -> {
                    var was = setLabelOffset(label, offset);
                    assert over <= was.over();
                    over = was.over();
                    builder.with(element);
                }
                case BranchInstruction br when br.opcode() == Opcode.GOTO -> {
                    var target = br.target();
                    boolean small = isSmallBranch(target, offset, over);
                    if (small) {
                        builder.with(br);
                        offset += Opcode.GOTO.sizeIfFixed();
                        over += Instructions.largeBranchAdjustment(br, offset);
                    } else {
                        builder.goto_w(br.target());
                        offset += Opcode.GOTO_W.sizeIfFixed();                        
                    }
                }                            
                case BranchInstruction brif -> {
                    var opcode = brif.opcode();
                    assert opcode != Opcode.GOTO_W;
                    var target = brif.target();
                    boolean small = isSmallBranch(target, offset, over);
                    if (small) {
                        builder.with(brif);
                        offset += opcode.sizeIfFixed();
                        over += Instructions.largeBranchAdjustment(brif, offset);
                    } else {
                        var opcodex = Opcodes.oppositeBranch(opcode);
                        Label after = builder.newLabel();
                        builder.with(BranchInstruction.of(opcodex, after))
                            .goto_w(target)
                            .labelBinding(after);
                        offset += opcodex.sizeIfFixed() + Opcode.GOTO_W.sizeIfFixed();
                    }
                }
                case DiscontinuedInstruction.JsrInstruction jsr ->  {
                    Label target = jsr.target();
                    boolean small = isSmallBranch(target, offset, over);
                    if (small) {
                        builder.with(jsr);
                        offset += Opcode.JSR.sizeIfFixed();
                        over += Instructions.largeBranchAdjustment(jsr, offset);
                    } else {
                        builder.with(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR_W, target));
                        offset += Opcode.JSR_W.sizeIfFixed();                        
                    }
                }
                case Instruction inst -> {
                    builder.with(inst);
                    offset += Instructions.sizeOfAt(inst, offset);
                }
                case null -> throw new NullPointerException();
                default -> builder.with(element);
            }
        }
    }

    private Offset setLabelOffset(Label label, int offset) {
        var was = offsetMap.get(label);
        offsetMap.put(label, was.actual(offset));
        return was;
    }
    
    private boolean isSmallBranch(Label target, int offset, int over) {
        if (offset < 0 || offset > MAX_OFFSET) {
            // "offset %d is not in range [%s,%s]"
            throw new LogIllegalArgumentException(M644, offset, 0, MAX_OFFSET);
        }
        var laboff = offsetMap.get(target); 
        return laboff.small(offset, over);
    }

}

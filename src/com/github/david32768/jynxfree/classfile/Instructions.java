package com.github.david32768.jynxfree.classfile;

import static java.lang.classfile.Opcode.*;

import java.lang.classfile.Instruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.Opcode;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.my.Message.M629;
import static com.github.david32768.jynxfree.my.Message.M630;
import static com.github.david32768.jynxfree.my.Message.M631;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public class Instructions {
    
    public enum OperationType {
       ARRAY_LENGTH(ARRAYLENGTH),
       UNARY(INEG, LNEG, FNEG, DNEG),
       SHIFT(ISHL, ISHR, IUSHR, LSHL, LSHR, LUSHR),
       COMPARE(LCMP, FCMPG, FCMPL, DCMPG, DCMPL),
       BINARY(IADD, LADD, FADD, DADD,
                ISUB, LSUB, FSUB, DSUB,
                IMUL, LMUL, FMUL, DMUL,
                IDIV, LDIV, FDIV, DDIV,
                IREM, LREM, FREM, DREM,
                IAND, LAND, IOR, LOR, IXOR, LXOR),
       ;
       
       private final EnumSet<Opcode> opcodes;

        private OperationType(Opcode opcode1, Opcode... opcodes) {
            this.opcodes = EnumSet.of(opcode1, opcodes);
        }
        
        public static OperationType of(OperatorInstruction opinst) {
            Opcode op = opinst.opcode();
            return Stream.of(values())
                    .filter(ot -> ot.opcodes.contains(op))
                    .findAny()
                    .orElseThrow();
        }
    }

    public enum BranchType {
        COMMAND(GOTO, GOTO_W),
        AUNARY(IFNULL, IFNONNULL),
        IUNARY(IFEQ, IFNE, IFLT, IFGT, IFLE, IFGE),
        ABINARY(IF_ACMPEQ, IF_ACMPNE),
        IBINARY(IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGT, IF_ICMPGE, IF_ICMPLE),
        ;

        private final EnumSet<Opcode> opcodes;

        private BranchType(Opcode opcode1, Opcode... opcodes) {
            this.opcodes = EnumSet.of(opcode1, opcodes);
        }
        
        public static BranchType of(BranchInstruction opinst) {
            Opcode op = opinst.opcode();
            return Stream.of(values())
                    .filter(ot -> ot.opcodes.contains(op))
                    .findAny()
                    .orElseThrow();
            
        }

    }

    private Instructions() {}
    
    private static final int MAX_CODESIZE = 2*Short.MAX_VALUE + 1; // unsigned short max value
        
    public static int sizeOfAt(Instruction instruction, int offset) {
        var op = instruction.opcode();
        return switch(instruction) {
            case LookupSwitchInstruction swinst -> 
                getAndCheckLookupSwitchSize(swinst.cases(), offset);
            case TableSwitchInstruction swinst -> 
                getAndCheckTableSwitchSize(swinst.lowValue(), swinst.highValue(), offset);
            default -> op.sizeIfFixed();
        };
    }

    private static int getAndCheckLookupSwitchSize(List<SwitchCase> cases, int offset) {
        long size = getLookupSwitchSize(cases, offset);
        if (size > MAX_CODESIZE) {
            // "number of cases %d is too large at any offset"
            throw new LogIllegalArgumentException(M629, cases.size());                    
        }
        return (int)size;
    }

    static long getLookupSwitchSize(List<SwitchCase> cases, int offset) {
        int padding = 3 - (offset & 3);
        return 1L + padding + 4 + 4 + 8L*cases.size();
    }
    
    private static int getAndCheckTableSwitchSize(int low, int high, int offset) {
        long size = getTableSwitchSize(low, high, offset);
        if (size > MAX_CODESIZE) {
            // "range [%d, %d] is too large at any offset"
            throw new LogIllegalArgumentException(M630, low, high);
        }
        return (int)size;
    }
    
    static long getTableSwitchSize(int low, int high, int offset) {
        int padding = 3 - (offset & 3);
        long cases = 1L + high - low;
        if (cases <= 0) {
            // "low(%d) > high(%d)"
            throw new LogIllegalArgumentException(M631 , low, high);
        }
        return 1L + padding + 4 + 4 + 4 + 4*cases;
    }

    private static final int GOTO_W_SIZE = Opcode.GOTO_W.sizeIfFixed();
    private static final int JSR_WIDEN_COST = Opcode.JSR_W.sizeIfFixed() - Opcode.JSR.sizeIfFixed();
    private static final int GOTO_WIDEN_COST = Opcode.GOTO_W.sizeIfFixed() - Opcode.GOTO.sizeIfFixed();
    
    public static int largeBranchAdjustment(Instruction instruction, int offset) {
        var op = instruction.opcode();
        int padding = 3 - (offset & 3);
        int extrapadding = 3 - padding; // in case long branches converted to short
        return switch(op) {
            case TABLESWITCH -> extrapadding;
            case LOOKUPSWITCH -> extrapadding;
            case JSR -> JSR_WIDEN_COST;
            case JSR_W -> 0;
            case GOTO -> GOTO_WIDEN_COST;
            case GOTO_W -> 0;
            default -> instruction instanceof BranchInstruction? GOTO_W_SIZE: 0;
        };
    }

    public static Optional<Integer> slot(Instruction inst) {
        Integer slot = switch(inst) {
            case StoreInstruction store -> store.slot();
            case LoadInstruction load -> load.slot();
            case IncrementInstruction incr -> incr.slot();
            case DiscontinuedInstruction.RetInstruction ret -> ret.slot();
            default -> null;
        };
        return Optional.ofNullable(slot);
    }

}

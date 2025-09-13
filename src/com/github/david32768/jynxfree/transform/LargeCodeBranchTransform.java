package com.github.david32768.jynxfree.transform;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.MethodModel;
import java.util.function.Function;
import java.util.Optional;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.my.Message.M645;

import com.github.david32768.jynxfree.classfile.SmallInstructions;

public class LargeCodeBranchTransform implements CodeTransform {

    private static final int MAX_OFFSET = 65535;
 
    private final Optional< MethodModel> method;
    private CodeArrayBuilder lcb;

    private LargeCodeBranchTransform(MethodModel method) {
        this.method = Optional.ofNullable(method);
    }

    public static Function<MethodModel, CodeTransform> supplier() {
        return LargeCodeBranchTransform::new;
    }

    public static CodeTransform of() {
        return new LargeCodeBranchTransform(null);
    }
    
    @Override
    public void atStart(CodeBuilder builder) {
        lcb = CodeArrayBuilder.of();
    }
    
    @Override
    public void accept(CodeBuilder builder, CodeElement element) {
        element = switch(element) {
            case ConstantInstruction.IntrinsicConstantInstruction _ ->
                element;
            case ConstantInstruction coninst ->
                SmallInstructions.smallConst(coninst);
            case LookupSwitchInstruction lookup ->
                SmallInstructions.smallestSwitch(lookup);
            case TableSwitchInstruction table ->
                SmallInstructions.smallestSwitch(table);
            default -> element;
        };
        lcb.add(element);
    }

    @Override
    public void atEnd(CodeBuilder builder) {
        if (lcb.maxSize() > MAX_OFFSET) {
            // "potential maximum size %d of method %s is larger than the maximum %d"
            LOG(M645,lcb.maxSize(), methodName(), MAX_OFFSET);
        }
        lcb.atEnd(builder);
    }

    private String methodName() {
        String name = method.map(mm -> mm.methodName().stringValue()).orElse("**unknown**");
        String desc = method.map(mm -> mm.methodType().stringValue()).orElse("");
        return name + desc;
    }
}

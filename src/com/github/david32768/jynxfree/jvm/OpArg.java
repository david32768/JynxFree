package com.github.david32768.jynxfree.jvm;

import java.lang.classfile.Opcode;
import java.util.EnumSet;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jvm.ConstantPoolType.*;
import static com.github.david32768.jynxfree.jvm.OpPart.*;

public enum OpArg {
    
    arg_atype(2, TYPE),
    arg_byte(2, BYTE),
    arg_callsite(5, CONSTANT_InvokeDynamic, CP, ZERO, ZERO),
    arg_class(3, CONSTANT_Class, CP),
    arg_constant(2, OpArg.getLoadable(), CP),
    arg_dir(0),    // pseudo instructions e.g. .line
    arg_field(3, CONSTANT_Fieldref, CP),
    arg_incr(3, VAR, INCR),
    arg_interface(5, CONSTANT_InterfaceMethodref, CP, UBYTE, ZERO),
    arg_label(3, LABEL),
    arg_marray(4, CONSTANT_Class, CP, UBYTE),
    arg_method(3, EnumSet.of(CONSTANT_Methodref, CONSTANT_InterfaceMethodref), CP),
    arg_none(1),
    arg_short(3, SHORT),
    arg_stack(1),
    arg_switch(null, SWITCH),
    arg_var(2, VAR),
    ;

    private final Integer length;
    private final EnumSet<ConstantPoolType> cptypes;
    private final OpPart[] parts;

    private OpArg(Integer length) {
        this(length, EnumSet.noneOf(ConstantPoolType.class));
    }

    private OpArg(Integer length, OpPart... parts) {
        this(length, EnumSet.noneOf(ConstantPoolType.class), parts);
    }

    private OpArg(Integer length, ConstantPoolType first) {
        this(length, EnumSet.of(first));
    }

    private OpArg(Integer length, ConstantPoolType first, OpPart... parts) {
        this(length, EnumSet.of(first),parts);
    }

    private OpArg(Integer length, EnumSet<ConstantPoolType> cptypes, OpPart... parts) {
        this.length = length;
        this.cptypes = cptypes;
        this.parts = parts;
        assert !cptypes.isEmpty() == (parts.length > 0 && parts[0] == CP):name();
    }

    public Integer length() {
        return length;
    }
    
    public boolean hasCPEntry() {
        return !cptypes.isEmpty();
    }

    public OpPart[] getParts() {
        return parts.clone();
    }
    
    public void checkCPType(ConstantPoolType actual) {
        actual.checkCPType(cptypes);
    }
    
    private static EnumSet<ConstantPoolType> getLoadable() {
        EnumSet<ConstantPoolType>  cpset = EnumSet.noneOf(ConstantPoolType.class);
        Stream.of(ConstantPoolType.values())
                .filter(cpt -> cpt.isLoadableBy(JvmVersion.MAX_VERSION))
                .forEach(cpt -> cpset.add(cpt));
        return cpset;
    }
    
    @Override
    public String toString() {
        return name().substring(4);
    }

    public static OpArg of(Opcode opcode) {
        return switch(opcode.kind()) {
            case ARRAY_LOAD -> arg_none;
            case ARRAY_STORE -> arg_none;
            case BRANCH -> arg_label;
            case CONSTANT -> 
                switch(opcode) {
                    case BIPUSH -> arg_byte;
                    case SIPUSH -> arg_short;
                    case LDC, LDC_W, LDC2_W -> arg_constant;    
                    default -> arg_none;
                };
            case CONVERT -> arg_none;
            case DISCONTINUED_JSR -> arg_label;
            case DISCONTINUED_RET -> arg_var;
            case FIELD_ACCESS -> arg_field;
            case INCREMENT -> arg_incr;
            case INVOKE -> opcode == Opcode.INVOKEINTERFACE? arg_interface: arg_method;
            case INVOKE_DYNAMIC -> arg_callsite;
            case LOAD -> arg_var;
            case LOOKUP_SWITCH -> arg_switch;
            case MONITOR -> arg_none;
            case NEW_MULTI_ARRAY -> arg_marray;
            case NEW_OBJECT -> arg_class;
            case NEW_PRIMITIVE_ARRAY -> arg_atype;
            case NEW_REF_ARRAY -> arg_class;
            case NOP -> arg_none;
            case OPERATOR -> arg_none;
            case RETURN -> arg_none;
            case STACK -> arg_stack;
            case STORE -> arg_var;
            case TABLE_SWITCH -> arg_switch;
            case THROW_EXCEPTION -> arg_none;
            case TYPE_CHECK -> arg_class;
        };
    }
}

package com.github.david32768.jynxfree.jvm;

import java.lang.classfile.Opcode;
import java.lang.invoke.MethodHandleInfo;
import java.util.EnumSet;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jynx.Message.*;
import static com.github.david32768.jynxfree.jynx.Message.M101;
import static com.github.david32768.jynxfree.jynx.Message.M76;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public enum HandleType {
    
    // jvms 4.4.8
    REF_getField("GF", MethodHandleInfo.REF_getField,1,
            Opcode.GETFIELD,ConstantPoolType.CONSTANT_Fieldref),
    REF_getStatic("GS", MethodHandleInfo.REF_getStatic,2,
            Opcode.GETSTATIC,ConstantPoolType.CONSTANT_Fieldref),
    REF_putField("PF", MethodHandleInfo.REF_putField,3,
            Opcode.PUTFIELD,ConstantPoolType.CONSTANT_Fieldref),
    REF_putStatic("PS", MethodHandleInfo.REF_putStatic,4,
            Opcode.PUTSTATIC,ConstantPoolType.CONSTANT_Fieldref),
    REF_invokeVirtual("VL", MethodHandleInfo.REF_invokeVirtual,5,
            Opcode.INVOKEVIRTUAL,ConstantPoolType.CONSTANT_Methodref),
    REF_invokeStatic("ST", MethodHandleInfo.REF_invokeStatic,6,
            Opcode.INVOKESTATIC,ConstantPoolType.CONSTANT_Methodref,
            Feature.invokestatic_interface,ConstantPoolType.CONSTANT_InterfaceMethodref),
    REF_invokeSpecial("SP", MethodHandleInfo.REF_invokeSpecial,7,
            Opcode.INVOKESPECIAL,ConstantPoolType.CONSTANT_Methodref,
            Feature.invokespecial_interface,ConstantPoolType.CONSTANT_InterfaceMethodref),
    REF_newInvokeSpecial("NW", MethodHandleInfo.REF_newInvokeSpecial,8,
            Opcode.INVOKESPECIAL,ConstantPoolType.CONSTANT_Methodref),
    REF_invokeInterface("IN", MethodHandleInfo.REF_invokeInterface,9,
            Opcode.INVOKEINTERFACE,ConstantPoolType.CONSTANT_InterfaceMethodref),
    ;
    
    private final String mnemonic;
    private final int reftype;
    private final Opcode opcode;
    private final ConstantPoolType maincpt;
    private final Feature altfeature;
    private final ConstantPoolType altcpt;
    
    private HandleType(String mnemonic,int reftype, int refnum, Opcode opcode, ConstantPoolType maincpt) {
        this(mnemonic, reftype, refnum, opcode, maincpt, Feature.never, null);
    }
    
    private HandleType(String mnemonic,int reftype, int refnum, Opcode opcode, ConstantPoolType maincpt,
            Feature altfeature, ConstantPoolType altcpt) {
        this.mnemonic = mnemonic;
        // "%s: jynx value (%d) does not agree with classfile value(%d)"
        assert reftype == refnum:M161.format(name(), refnum, reftype);
        assert reftype == 1 + this.ordinal();
        this.reftype = reftype;
        this.opcode = opcode;
        this.maincpt = maincpt;
        this.altfeature = altfeature;
        this.altcpt = altcpt;
        assert ordinal() == reftype - 1;
        assert maincpt != null;
    }

    private String getMnemonic() {
        return mnemonic;
    }

    public String getPrefix() {
        return mnemonic + SEP;
    }
    
    public Opcode opcode() {
        return opcode;
    }

    public int reftype() {
        return reftype;
    }

    public EnumSet<ConstantPoolType>  getValidCPT(JvmVersion jvmversion) {
        if (altcpt != null && jvmversion.supports(altfeature)) {
            return EnumSet.of(maincpt,altcpt);
        }
        return EnumSet.of(maincpt);
    }
    
    public boolean isField() {
        return maincpt == ConstantPoolType.CONSTANT_Fieldref;
    }
    
    public boolean maybeOK(HandleType other) {
        return this == other
                || this == REF_invokeSpecial && other == REF_invokeVirtual
                || other == REF_invokeSpecial && this == REF_invokeVirtual;
    }
    
    @Override
    public String toString() {
        return String.format("%s: (=%s)",mnemonic,name().replace("REF_",""));
    }

    public static HandleType getInstance(int reftype) {
        return Stream.of(values())
                .filter(ht -> ht.reftype == reftype)
                .findFirst()
                .orElseThrow(()-> new LogIllegalArgumentException(M76,reftype)); // "unknown handle tag: %d"
    }

    public static HandleType fromMnemonic(String mnemonic) {
        return Stream.of(values())
                .filter(ht -> ht.mnemonic.equals(mnemonic))
                .findFirst()
                .orElseThrow(()-> new LogIllegalArgumentException(M101,mnemonic)); // "unknown handle mnemonic: %s"
    }

    public static HandleType fromOp(Opcode opcode, boolean init) {
        if (init && opcode == Opcode.INVOKESPECIAL) {
            return REF_newInvokeSpecial;
        }
        return Stream.of(values())
                .filter(ht -> ht.opcode() == opcode)
                .findFirst()
                .get();
    }

    public final static char SEP = ':';
    
    public static String getPrefix(int tag) {
        HandleType ht = getInstance(tag); 
        String htype = ht.getMnemonic();
        return htype + SEP;
    }

}

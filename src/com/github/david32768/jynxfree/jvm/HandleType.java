package com.github.david32768.jynxfree.jvm;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC_GETTER;

import java.lang.classfile.Opcode;
import java.lang.constant.DirectMethodHandleDesc.Kind;
import java.util.EnumSet;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jvm.ConstantPoolType.*;

import static com.github.david32768.jynxfree.my.Message.M101;
import static com.github.david32768.jynxfree.my.Message.M161;
import static com.github.david32768.jynxfree.my.Message.M76;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public enum HandleType {
    
    // jvms 4.4.8
    REF_getField("GF", 1, Opcode.GETFIELD, Kind.GETTER),
    REF_getStatic("GS", 2, Opcode.GETSTATIC, Kind.STATIC_GETTER),
    REF_putField("PF", 3, Opcode.PUTFIELD, Kind.SETTER),
    REF_putStatic("PS", 4, Opcode.PUTSTATIC, Kind.STATIC_SETTER),
    REF_invokeVirtual("VL", 5, Opcode.INVOKEVIRTUAL, Kind.VIRTUAL),
    REF_invokeStatic("ST", 6, Opcode.INVOKESTATIC, Kind.STATIC,
            Feature.invokestatic_interface, Kind.INTERFACE_STATIC),
    REF_invokeSpecial("SP", 7, Opcode.INVOKESPECIAL, Kind.SPECIAL,
            Feature.invokespecial_interface, Kind.INTERFACE_SPECIAL),
    REF_newInvokeSpecial("NW", 8, Opcode.INVOKESPECIAL, Kind.CONSTRUCTOR),
    REF_invokeInterface("IN", 9, Opcode.INVOKEINTERFACE, Kind.INTERFACE_VIRTUAL),
    ;
    
    private final String mnemonic;
    private final int reftype;
    private final Opcode opcode;
    private final ConstantPoolType maincpt;
    private final Feature altfeature;
    private final ConstantPoolType altcpt;
    
    private HandleType(String mnemonic, int refnum, Opcode opcode, Kind kind) {
        this(mnemonic, refnum, opcode, kind, Feature.never, null);
    }
    
    private HandleType(String mnemonic, int refnum, Opcode opcode, Kind kind,
            Feature altfeature, Kind altkind) {
        this.mnemonic = mnemonic;
        this.reftype = kind.refKind;
        // "%s: jynx value (%d) does not agree with classfile value(%d)"
        assert this.reftype == refnum:M161.format(name(), refnum, reftype);
        assert this.reftype == 1 + this.ordinal();
        this.opcode = opcode;
        this.maincpt = getConstantPoolType(kind);
        this.altfeature = altfeature;
        this.altcpt = altkind == null? null: getConstantPoolType(altkind);
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

    private static ConstantPoolType getConstantPoolType(Kind kind) {
        return switch(kind) {
            case CONSTRUCTOR, SPECIAL, STATIC, VIRTUAL -> CONSTANT_Methodref;
            case GETTER, SETTER, STATIC_GETTER,STATIC_SETTER -> CONSTANT_Fieldref;
            case INTERFACE_SPECIAL, INTERFACE_STATIC, INTERFACE_VIRTUAL -> CONSTANT_InterfaceMethodref;
        };
    }
}

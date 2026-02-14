package com.github.david32768.jynxfree.classfile;

import static java.lang.classfile.Opcode.*;

import java.lang.classfile.Opcode;
import java.util.Optional;


public enum SmallOpcodeType {

    MINUS1(ICONST_M1),
    PLUS0(ICONST_0, FCONST_0, LCONST_0, DCONST_0),
    PLUS1(ICONST_1, FCONST_1, LCONST_1, DCONST_1),
    PLUS2(ICONST_2, FCONST_2),
    PLUS3(ICONST_3),
    PLUS4(ICONST_4),
    PLUS5(ICONST_5),
    BYTE(BIPUSH),
    SHORT(SIPUSH),
    INT(LDC),
    ;
    
    private final Opcode iop;
    private final Opcode fop;
    private final Opcode lop;
    private final Opcode dop;


    private SmallOpcodeType(Opcode iop) {
        this(iop, Opcode.LDC);
    }

    private SmallOpcodeType(Opcode iop, Opcode fop) {
        this(iop, fop, Opcode.LDC2_W, Opcode.LDC2_W);
    }

    private SmallOpcodeType(Opcode iop, Opcode fop, Opcode lop, Opcode dop) {
        this.iop = iop;
        this.fop = fop;
        this.lop = lop;
        this.dop = dop;
    }

    public Opcode iop() {
        return iop;
    }

    public Opcode fop() {
        return fop;
    }

    public Opcode lop() {
        return lop;
    }

    public Opcode dop() {
        return dop;
    }

    public static SmallOpcodeType of(int i) {
        return switch(i) {
            case -1 -> MINUS1;
            case 0 -> PLUS0;
            case 1 -> PLUS1;
            case 2 -> PLUS2;
            case 3 -> PLUS3;
            case 4 -> PLUS4;
            case 5 -> PLUS5;
            default -> {
                if (i == (byte)i) {
                    yield BYTE;
                } else if (i == (short)i) {
                    yield SHORT;
                } else {
                    yield INT;
                }
            }
        };
    }
    
    public static Opcode opFor(int i) {
        return of(i).iop;
    }
    
    public static Opcode opFor(long j) {
        int i = (int)j;
        return j == i? of(i).lop: LDC2_W;
    }
    
    public static Optional<Opcode> intOpFor(long j) {
        Opcode lop = opFor(j);
        int i = (int)j;
        if (j == i) {
            var type = of(i);
            var iop = type.iop;
            if (type != INT && iop.sizeIfFixed() < lop.sizeIfFixed()) {
                return Optional.of(iop);
            }
        }
        return Optional.empty();
    }
    
    public static Opcode opFor(float f) {
        int i = (int)f;
        return Float.compare(f, (float)i) == 0? of(i).fop: LDC;
    }
    
    public static Optional<Opcode> intOpFor(float f) {
        Opcode fop = opFor(f);
        int i = (int)f;
        if (Float.compare(f, (float)i) == 0) { // exclude -0.0F
            var type = of(i);
            var iop = type.iop;
            if (type != INT && iop.sizeIfFixed() < fop.sizeIfFixed()) {
                return Optional.of(iop);
            }
        }
        return Optional.empty();
    }
    
    public static Opcode opFor(double d) {
        int i = (int)d;
        return Double.compare(d, (double)i) == 0? of(i).dop: LDC2_W;
    }
    
    public static Optional<Opcode> intOpFor(double d) {
        Opcode dop = opFor(d);
        int i = (int)d;
        if (Double.compare(d, (double)i) == 0) { // exclude -0.0D
            var type = of(i);
            var iop = type.iop;
            if (type != INT && iop.sizeIfFixed() < dop.sizeIfFixed()) {
                return Optional.of(iop);
            }
        }
        return Optional.empty();
    }
    
}

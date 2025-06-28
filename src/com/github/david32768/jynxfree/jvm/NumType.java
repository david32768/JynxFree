package com.github.david32768.jynxfree.jvm;

import java.lang.classfile.TypeKind;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.my.Message.M129;
import static com.github.david32768.jynxfree.my.Message.M70;
import static com.github.david32768.jynxfree.my.Message.M77;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public enum NumType {

    t_boolean(TypeKind.BOOLEAN, 'Z', 4, 0, 1),
    t_byte(TypeKind.BYTE, 'B', 8, Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.toUnsignedLong((byte)-1)),
    t_char(TypeKind.CHAR, 'C', 5, Character.MIN_VALUE, Character.MAX_VALUE),
    t_short(TypeKind.SHORT, 'S', 9, Short.MIN_VALUE, Short.MAX_VALUE, Short.toUnsignedLong((short)-1)),
    t_int(TypeKind.INT, 'I', 10, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.toUnsignedLong(-1)),
    t_long(TypeKind.LONG, 'J', 11,  Long.MIN_VALUE, Long.MAX_VALUE),
    t_float(TypeKind.FLOAT, 'F', 6),
    t_double(TypeKind.DOUBLE, 'D', 7),
    ;

    private final String classtype;
    private final TypeKind typekind;
    private final long minvalue;
    private final long maxvalue;
    private final long unsignedMaxvalue;
    

    private NumType(TypeKind typekind, char classtype, int typecode) {
        this(typekind, classtype, typecode, 0, 0, 0);
    }
    
    private NumType(TypeKind typekind, char classtype, int typecode, long minvalue, long maxvalue) {
        this(typekind, classtype, typecode,minvalue, maxvalue, maxvalue);
    }

    private NumType(TypeKind typekind, char classtype, int typecode, long minvalue, long maxvalue, long unsignedmaxvalue) {
        assert name().startsWith("t_");
        assert Character.isUpperCase(classtype);
        this.classtype = "" + classtype;
        assert typekind.newarrayCode() == typecode;
        this.typekind = typekind;
        this.minvalue = minvalue;
        this.maxvalue = maxvalue;
        this.unsignedMaxvalue = unsignedmaxvalue;
        assert maxvalue == unsignedmaxvalue || unsignedmaxvalue == 2*maxvalue + 1:name();
    }

    public int typecode() {
        return typekind.newarrayCode();
    }

    public String classType() {
        return classtype;
    }

    public long unsignedMaxvalue() {
        return unsignedMaxvalue;
    }

    public String externalName() {
        return name().substring(2);
    }
    
    public static NumType getInstance(int typecode) {
        return Stream.of(values())
                .filter(nt->nt.typecode() == typecode)
                .findFirst()
                // "invalid typecode - %d" 
                .orElseThrow(()->new LogIllegalArgumentException(M129,typecode));
    }
    
    @Override
    public String toString() {
        return externalName();
    }
    
    public static Optional<NumType> fromString(String token) {
        return Stream.of(values())
                .filter(nt->token.equals(nt.externalName()) || token.equals(nt.classtype))
                .findFirst();
    }

    public boolean isInRange(long var) {
        if (maxvalue == 0) {
            // "cannot range check floating point numbers"
            throw new LogIllegalArgumentException(M70);
        }
        return var >= minvalue && var <= maxvalue;
    }

    // NB long is always signed
    public boolean isInUnsignedRange(long var) {
        if (maxvalue == 0) {
            // "cannot range check floating point numbers"
            throw new LogIllegalArgumentException(M70);
        }
        return var >= 0 && var <= unsignedMaxvalue;
    }

    public void checkInRange(long var) {
        if (!isInRange(var)) {
            // "%s value %d is not in range [%d,%d]"
            String msg = M77.format(this, var, minvalue, maxvalue);
            throw new NumberFormatException(msg);
        }
    }
    
    public void checkInUnsignedRange(long var) {
        if (!isInUnsignedRange(var)) {
            // "%s value %d is not in range [%d,%d]"
            String msg = M77.format(this, var, 0, 1+2*maxvalue); 
            throw new NumberFormatException(msg);
        }
    }
    
}

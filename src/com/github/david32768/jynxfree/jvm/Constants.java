package com.github.david32768.jynxfree.jvm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumSet;

public enum Constants {
    
    OBJECT_CLASS(Object.class),
    STRING_CLASS(String.class),
    CLASS_CLASS(Class.class),
    RECORD_SUPER("java/lang/Record"),
    ENUM_SUPER(Enum.class),
    OBJECT_INPUT_STREAM(ObjectInputStream.class),
    OBJECT_OUTPUT_STREAM(ObjectOutputStream.class),
    
    JAVA_BASE_MODULE("java.base"),
    MODULE_CLASS_NAME("module-info"),
    PACKAGE_INFO_NAME("package-info"),
    
    EQUALS("equals(L%s;)Z",OBJECT_CLASS),
    HASHCODE("hashCode()I"),
    TOSTRING("toString()L%s;",STRING_CLASS),
    GETCLASS("getClass()L%s;",CLASS_CLASS),
    CLONE("clone()L%s;",OBJECT_CLASS),
    NOTIFY("notify()V"),
    NOTIFYALL("notifyAll()V"),
    WAIT("wait()V"),
    WAITJ("wait(J)V"),
    WAITJI("wait(JI)V"),
    FINALIZE("finalize()V"),
    
    CLASS_INIT_NAME("<init>"),
    STATIC_INIT_NAME("<clinit>"),
    STATIC_INIT("<clinit>()V"),

    NAME("name()L%s;",STRING_CLASS),
    ORDINAL("ordinal()I"),
    VALUES_FORMAT("%1$s.values()[L%1$s;"),
    VALUEOF_FORMAT("%%1$s.valueOf(L%s;)L%%1$s;",STRING_CLASS),
    GET_DECLARING_CLASS("getDeclaringClass()L%s;",CLASS_CLASS),
    COMPARETO_FORMAT("compareTo(L%s;)I"),
    
    READ_OBJECT("readObject(L%s;)V",OBJECT_INPUT_STREAM),
    WRITE_OBJECT("writeObject(L%s;)V",OBJECT_OUTPUT_STREAM),
    READ_OBJECT_NODATA("readObjectNoData()V"),

    ;
    
    private final String str;

    private Constants(Class<?> klass) {
        this(klass.getName().replace(".","/"));
    }
    
    private Constants(String str) {
        this.str = str;
    }
    
    private Constants(String str, Object... objs) {
        this.str = String.format(str,objs);
    }

    public String internalName() {
        return str.startsWith("L") && str.endsWith(";")?str.substring(1, str.length() - 1):str;
    }
    
    public String regex() {
        return str.replace("(", "\\(").replace(")", "\\)");
    }
    
    public boolean equalsString(String other) {
        return stringValue().equals(other);
    }

    public String stringValue() {
        return str;
    }

    @Override
    public String toString() {
        return str;
    }
    
    public static boolean isObjectClass(String classname) {
        return OBJECT_CLASS.equalsString(classname);
    }

    public static final EnumSet<Constants> ARRAY_METHODS
            = EnumSet.of(CLONE,EQUALS,HASHCODE,TOSTRING,GETCLASS,NOTIFY,NOTIFYALL,WAIT,WAITJ,WAITJI);

    public static final EnumSet<Constants> FINAL_OBJECT_METHODS
            = EnumSet.of(GETCLASS,NOTIFY,NOTIFYALL,WAIT,WAITJ,WAITJI);

    // excluding COMPARETO which requires class name
    public static final EnumSet<Constants> FINAL_ENUM_METHODS
            = EnumSet.of(NAME,ORDINAL,EQUALS,HASHCODE,CLONE,GET_DECLARING_CLASS,FINALIZE);

    public static final EnumSet<Constants> PRIVATE_SERIALIZATION_METHODS
            = EnumSet.of(READ_OBJECT,WRITE_OBJECT,READ_OBJECT_NODATA);

    public static final EnumSet<Constants>  INVALID_COMPONENTS // ()V object methods
            = EnumSet.of(CLONE,FINALIZE,GETCLASS,HASHCODE,NOTIFY,NOTIFYALL,TOSTRING,WAIT);

    public static boolean isNameIn(String str, EnumSet<Constants> set) {
        return set.stream().anyMatch(cnst->cnst.name().equalsIgnoreCase(str));
    }
    
    public static final int MAX_CODE = (int)NumType.t_short.unsignedMaxvalue();

}

package com.github.david32768.jynxfree.jvm;

import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jynx.Message.*;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

import com.github.david32768.jynxfree.jynx.LogAssertionError;

public enum FrameType {

    // jvms 4.7.4
    ft_Top(0, VerificationTypeInfo.ITEM_TOP),
    ft_Integer(1, VerificationTypeInfo.ITEM_INTEGER),
    ft_Float(2, VerificationTypeInfo.ITEM_FLOAT),
    ft_Double(3, VerificationTypeInfo.ITEM_DOUBLE),
    ft_Long(4, VerificationTypeInfo.ITEM_LONG),
    ft_Null(5, VerificationTypeInfo.ITEM_NULL),
    ft_UninitializedThis(6, VerificationTypeInfo.ITEM_UNINITIALIZED_THIS),
    ft_Object(7, VerificationTypeInfo.ITEM_OBJECT, true),
    ft_Uninitialized(8, VerificationTypeInfo.ITEM_UNINITIALIZED, true),
    ;

    private final int tag;
    private final Integer asmType;

    private FrameType(int tag, int classFileType) {
        this(tag, classFileType, false);
    }

    private FrameType(int tag, int classFileType, boolean more) {
        this.tag = tag;
        // "%s: jynx value (%d) does not agree with classfile value(%d)"
        assert tag == classFileType:M161.format(name(), tag, classFileType);
        this.asmType = more? null: classFileType;
    }

    public int tag() {
        return tag;
    }

    public boolean extra() {
        return asmType == null;
    }

    public Integer asmType() {
        return asmType;
    }

    public String externalName() {
        return name().substring(3);
    }
    
    @Override
    public String toString() {
        return externalName();
    }
    
    public static FrameType fromString(String token) {
        FrameType result = Stream.of(values())
                .filter(ft -> ft.externalName().equals(token))
                .findFirst()
                .orElse(ft_Top);
        if (result == ft_Top && !result.externalName().equals(token)) {
            LOG(M61,token,result);  // "invalid stack frame type(%s) - %s assumed"
        }
        return result;
    }
    
    public static FrameType fromAsmType(int type) {
        FrameType result =  Stream.of(values())
                .filter(ft -> ft.tag == type)
                .findFirst()
                .orElse(ft_Top);
        if (result.asmType == null || result == ft_Top && type != 0) {
            throw new LogAssertionError(M902,type); // "unknown ASM stack frame type (%d)"
        }
        return result;
    }
    
    public static FrameType fromJVMType(int type) {
        return Stream.of(values())
                .filter(ft -> ft.tag == type)
                .findFirst()
                .orElseThrow(() -> new LogAssertionError(M904,type)); // "unknown JVM stack frame type (%d)"
    }
    
}

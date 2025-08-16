package com.github.david32768.jynxfree.jvm;

import static java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo.*;

import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.my.Message.*;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

import com.github.david32768.jynxfree.jynx.LogAssertionError;

public enum FrameType {

    // jvms 4.7.4
    ft_Top(0, TOP),
    ft_Integer(1, INTEGER),
    ft_Float(2, FLOAT),
    ft_Double(3, DOUBLE),
    ft_Long(4, LONG),
    ft_Null(5, NULL),
    ft_Uninitialized_This(6, UNINITIALIZED_THIS),
    ft_Object(7, null, VerificationTypeInfo.ITEM_OBJECT),
    ft_Uninitialized(8, null, VerificationTypeInfo.ITEM_UNINITIALIZED),
    ;

    private final int tag;
    private final Integer asmType;

    private FrameType(int tag, SimpleVerificationTypeInfo type) {
        this(tag, type, type.tag());
    }

    private FrameType(int tag, SimpleVerificationTypeInfo type, int classFileType) {
        this.tag = tag;
        // "%s: jynx value (%d) does not agree with classfile value(%d)"
        assert tag == classFileType:M161.format(name(), tag, classFileType);
        this.asmType = type == null? null: classFileType;
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

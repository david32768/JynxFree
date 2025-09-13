package com.github.david32768.jynxfree.classfile;

import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.lang.classfile.attribute.StackMapTableAttribute;

import java.lang.classfile.Attributes;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackMap {
    
    public final static StackMap NONE = new StackMap(Collections.emptyMap(), null);
    
    private final Map<Label,StackMapFrameInfo> stackMapInfo;
    private final MethodModel method;

    public StackMap(Map<Label, StackMapFrameInfo> stackMapInfo, MethodModel method) {
        this.stackMapInfo = stackMapInfo;
        this.method = method;
    }
    

    public static StackMap of(MethodModel mm) {
        var infolist = frameInfoOf(mm);
        var info = getStackMap(infolist);
        return new StackMap(info, mm);
    }
    
    private static List<StackMapFrameInfo> frameInfoOf(MethodModel mm) {
        return mm.attributes().stream()
                .filter(attr -> attr instanceof CodeAttribute)
                .map(attr -> (CodeAttribute)attr)
                .flatMap(attr -> attr.findAttribute(Attributes.stackMapTable()).stream())
                .findAny()
                .map(StackMapTableAttribute::entries)
                .orElse(Collections.emptyList());        
    }
    
    private static Map<Label,StackMapFrameInfo> getStackMap(List<StackMapFrameInfo> infolist) {
        Map<Label,StackMapFrameInfo> stackmapinfo = new HashMap<>();
        for (var info : infolist) {
            var mustBeNull = stackmapinfo.put(info.target(), info);
            assert mustBeNull == null;
        }
        return stackmapinfo;
    }

    public static int slotSize(VerificationTypeInfo info) {
        return switch (info) {
            case SimpleVerificationTypeInfo.DOUBLE -> 2;
            case SimpleVerificationTypeInfo.LONG  -> 2;
            default -> 1;
        };        
    }
    
    public static TypeKind typeKind(VerificationTypeInfo info) {
        return switch (info) {
            case SimpleVerificationTypeInfo.DOUBLE -> TypeKind.DOUBLE;
            case SimpleVerificationTypeInfo.LONG  -> TypeKind.LONG;
            case SimpleVerificationTypeInfo.INTEGER -> TypeKind.INT;
            case SimpleVerificationTypeInfo.FLOAT -> TypeKind.FLOAT;
            case SimpleVerificationTypeInfo.TOP -> TypeKind.VOID;
            default -> TypeKind.REFERENCE;
        };        
    }
    
    public List<VerificationTypeInfo> initialLocals() {
        var parms = method.methodTypeSymbol().parameterList();
        List<StackMapFrameInfo.VerificationTypeInfo> result = new ArrayList<>();
        if (!method.flags().has(AccessFlag.STATIC)) {
            String mname = method.methodName().stringValue();
            if (mname.equals("<init>")) {
                result.add(SimpleVerificationTypeInfo.UNINITIALIZED_THIS);
            } else {
                var classDesc = method.parent().get().thisClass().asSymbol();
                result.add(verificationTypeInfoOf(classDesc));
            }
        }
        for (var parm : parms) {
            result.add(verificationTypeInfoOf(parm));
        }
        return result;
    }

    private static VerificationTypeInfo verificationTypeInfoOf(ClassDesc desc) {
        if (desc.isPrimitive()) {
            String primitive = desc.descriptorString();
            assert primitive.length() == 1;
            char first = primitive.charAt(0);
            return switch (first) {
                case 'J' -> SimpleVerificationTypeInfo.LONG;
                case 'F' -> SimpleVerificationTypeInfo.FLOAT;
                case 'D' -> SimpleVerificationTypeInfo.DOUBLE;
                default -> SimpleVerificationTypeInfo.INTEGER;
            };
        } else {
            return StackMapFrameInfo.ObjectVerificationTypeInfo.of(desc);
        }        
    }

    public List<VerificationTypeInfo> stackFrameFor(Label label) {
        var info = stackMapInfo.get(label);
        return info == null? null: info.stack();
    }
        
    public List<VerificationTypeInfo> localsFrameFor(Label label) {
        var info = stackMapInfo.get(label);
        return info == null? null: info.locals();
    }
        
}

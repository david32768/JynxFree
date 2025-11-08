package com.github.david32768.jynxfree.transform;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeKind;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SlotKind(int slot, TypeKind kind) {
    
    public SlotKind {
        if (slot < 0 || slot > 65535) {
            throw new IllegalArgumentException();
        }
        Objects.requireNonNull(kind);
    }
    
    public static List<SlotKind> ofParameters(MethodModel method) {
        return ofParameters(method.methodTypeSymbol(), method.flags().has(AccessFlag.STATIC));
    }
    
    public static List<SlotKind> ofParameters(MethodTypeDesc mtdesc, boolean isStatic) {
        List<SlotKind> slotlist = new ArrayList<>();
        if (!isStatic) {
            slotlist.add(new SlotKind(0, TypeKind.REFERENCE));
        }
        for (var klass : mtdesc.parameterList()) {
            int next = slotlist.size();
            TypeKind kind = TypeKind.fromDescriptor(klass.descriptorString());
            switch(kind) {
                case DOUBLE, LONG -> {
                    slotlist.add(new SlotKind(next, kind));
                    slotlist.add(new SlotKind(next + 1, TypeKind.VOID));
                }
                default -> {
                    slotlist.add(new SlotKind(next, kind));
                }
            }
        }
        return slotlist;
    }
    
}

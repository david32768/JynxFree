package com.github.david32768.jynxfree.transform;

import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeKind;
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
    
    public static List<SlotKind> OfParameters(MethodModel mm) {
        List slotlist = new ArrayList();
        int next = 0;
        if (!mm.flags().flags().contains(AccessFlag.STATIC)) {
            slotlist.add(new SlotKind(next,TypeKind.REFERENCE));
            ++next;
        }
        for (var klass : mm.methodTypeSymbol().parameterList()) {
            TypeKind kind = TypeKind.fromDescriptor(klass.descriptorString());
            slotlist.add(new SlotKind(next, kind));
            ++next;
            switch(kind) {
                case DOUBLE, LONG -> {
                    slotlist.add(new SlotKind(next, TypeKind.VOID));
                    ++next;
                }
                default ->{}
            }
        }
        return slotlist;
    }
    
}

package com.github.david32768.jynxfree.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.Optional;

public class Comparators {

    private Comparators(){}
    
    public static int compare(ExceptionCatch o1, ExceptionCatch o2) {
        var opt1 = o1.catchType().map(ClassEntry::asInternalName);
        var opt2 = o2.catchType().map(ClassEntry::asInternalName);
        int result = compareEmptyLast(opt1,opt2); // empty = all exceptions
        return result == 0?
                compareHash(o1, o2):
                result;
    }
    
    public static <T extends Comparable<T>> int compareEmptyLast(Optional<T> cmp1opt, Optional<T> cmp2opt) {
        if (cmp1opt.isEmpty() && cmp2opt.isEmpty()) {
            return 0;
        }
        if (cmp1opt.isEmpty()) {
            return 1;
        }
        if (cmp2opt.isEmpty()) {
            return -1;
        }
        return cmp1opt.get().compareTo(cmp2opt.get());
    }
    
    public static int compareHash(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }

}

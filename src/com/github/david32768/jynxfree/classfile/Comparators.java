package com.github.david32768.jynxfree.classfile;

import java.util.Optional;

public class Comparators {

    private Comparators(){}
    
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

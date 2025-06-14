package com.github.david32768.jynxfree.jvm;

public interface JvmVersioned {

    public default JvmVersionRange range(){
        return JvmVersionRange.UNLIMITED;
    }
    
}

package com.github.david32768.jynxfree.transform;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.attribute.SyntheticAttribute;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodTransform;
import java.lang.reflect.AccessFlag;

public class UpgradeSyntheticMethod implements MethodTransform {

    private AccessFlags savedFlags;
    private boolean hasAttribute;
    
    @Override
    public void accept(MethodBuilder builder, MethodElement element) {
        switch (element) {
            case AccessFlags flags -> savedFlags = flags;
            case SyntheticAttribute _ -> hasAttribute = true;
            default -> builder.with(element);
        }
    }

    @Override
    public void atEnd(MethodBuilder builder) {
        if (hasAttribute) {
            builder.withFlags(savedFlags.flagsMask() | AccessFlag.SYNTHETIC.mask());
        } else {
            builder.with(savedFlags);
        }
    }
    
}

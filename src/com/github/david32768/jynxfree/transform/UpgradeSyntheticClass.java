package com.github.david32768.jynxfree.transform;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.attribute.SyntheticAttribute;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.reflect.AccessFlag;

public class UpgradeSyntheticClass implements ClassTransform {

    private AccessFlags savedFlags;
    private boolean hasAttribute;
    
    @Override
    public void accept(ClassBuilder builder, ClassElement element) {
        switch (element) {
            case AccessFlags flags -> savedFlags = flags;
            case SyntheticAttribute _ -> hasAttribute = true;
            default -> builder.with(element);
        }
    }

    @Override
    public void atEnd(ClassBuilder builder) {
        if (hasAttribute) {
            builder.withFlags(savedFlags.flagsMask() | AccessFlag.SYNTHETIC.mask());
        } else {
            builder.with(savedFlags);
        }
    }
    
}

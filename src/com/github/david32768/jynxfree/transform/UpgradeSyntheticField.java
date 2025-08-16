package com.github.david32768.jynxfree.transform;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.attribute.SyntheticAttribute;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldTransform;
import java.lang.reflect.AccessFlag;

public class UpgradeSyntheticField implements FieldTransform {

    private AccessFlags savedFlags;
    private boolean hasAttribute;
    
    @Override
    public void accept(FieldBuilder builder, FieldElement element) {
        switch (element) {
            case AccessFlags flags -> savedFlags = flags;
            case SyntheticAttribute _ -> hasAttribute = true;
            default -> builder.with(element);
        }
    }

    @Override
    public void atEnd(FieldBuilder builder) {
        if (hasAttribute) {
            builder.withFlags(savedFlags.flagsMask() | AccessFlag.SYNTHETIC.mask());
        } else {
            builder.with(savedFlags);
        }
    }
    
}

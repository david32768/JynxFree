package com.github.david32768.jynxfree.transform;

import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileVersion;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;

import static com.github.david32768.jynxfree.my.Message.M625;

public class Transforms {

    private Transforms(){}

    private static final  ClassTransform ALL_METHODS
            = ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL);
    
    public static byte[] addStackMap(ClassFile classfile, ClassModel cm) {
        return addStackMap(classfile, cm, ALL_METHODS);
    }

    public static byte[] upgradeToAtLeastV6(ClassFile classfile, ClassModel cm) {
        int major = cm.majorVersion();
        if (major > ClassFile.JAVA_6_VERSION) {
            return classfile.transformClass(cm, ClassTransform.ACCEPT_ALL);
        }
        if (major == ClassFile.JAVA_6_VERSION && ClassModels.hasStackMap(cm)) {
            return classfile.transformClass(cm, ClassTransform.ACCEPT_ALL);
        }
        ClassTransform ct = upgradeToV6Transform(major);
        return Transforms.addStackMap(classfile, cm, ct);
    }
    
    public static ClassTransform upgradeToV6Transform(int major) {
        assert major <= ClassFile.JAVA_6_VERSION;
        ClassTransform ct = ALL_METHODS
                .andThen(Transforms::changeToV6)
                .andThen(ClassTransform.transformingMethods(Transforms::tidyThrows));
        if (major < ClassFile.JAVA_5_VERSION) {
            ct = ct.andThen(new UpgradeSyntheticClass())
                    .andThen(ClassTransform.transformingMethods(new UpgradeSyntheticMethod()))
                    .andThen(ClassTransform.transformingFields(new UpgradeSyntheticField()));
        }
        return ct;
    }
    
    private static byte[] addStackMap(ClassFile classfile, ClassModel cm, ClassTransform ct) {
        if (ClassModels.findDiscontinuedOpcode(cm).isPresent()) {
            // "one or more methods contain jsr/ret",
            throw new UnsupportedOperationException(M625.format());
        }
        byte[] bytes = classfile.withOptions(
                    ClassFile.StackMapsOption.GENERATE_STACK_MAPS,
        // PATCH_DEAD_CODE is the default but as it alters the bytecode it is deliberately added
                    ClassFile.DeadCodeOption.PATCH_DEAD_CODE)
                .transformClass(cm, ct);
        return bytes;
    }

    private static void changeToV6(ClassBuilder builder,ClassElement element) {
        switch (element) {
            case ClassFileVersion cfv -> {
                if (cfv.majorVersion() > ClassFile.JAVA_6_VERSION) {
                    throw new IllegalArgumentException();
                }
                builder.with(ClassFileVersion.of(ClassFile.JAVA_6_VERSION, 0));
            }
            default -> builder.with(element);
        }
    }
    
    private static void tidyThrows(MethodBuilder builder, MethodElement element) {
        switch (element) {
            case ExceptionsAttribute exattr when exattr.exceptions().isEmpty() -> {}
            default -> builder.with(element);            
        }
    }
}

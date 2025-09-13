package com.github.david32768.jynxfree.transform;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileVersion;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;

import static com.github.david32768.jynxfree.my.Message.M625;

import com.github.david32768.jynxfree.jynx.LogUnsupportedOperationException;

public class Transforms {

    private Transforms(){}

    private static final  ClassTransform ALL_METHODS
            = ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL);
    
    public static byte[] addStackMap(ClassFile classfile, ClassModel cm) {
        return addStackMap(classfile, cm, ALL_METHODS);
    }

    public static byte[] addStackMapForVerification(ClassFile classfile, ClassModel cm) {
        ClassTransform ct = ALL_METHODS
                .andThen(Transforms::changeToV7);
        return addStackMap(classfile, cm, ct);
    }

    private static byte[] addStackMap(ClassFile classfile, ClassModel cm, ClassTransform ct) {
        if (ClassModels.findDiscontinuedOpcode(cm).isPresent()) {
            // "one or more methods contain jsr/ret",
            throw new LogUnsupportedOperationException(M625);
        }
        byte[] bytes = classfile.withOptions(
                    ClassFile.StackMapsOption.GENERATE_STACK_MAPS,
        // PATCH_DEAD_CODE is the default but as it alters the bytecode it is deliberately added
                    ClassFile.DeadCodeOption.PATCH_DEAD_CODE)
                .transformClass(cm, ct);
        return bytes;
    }

    private static void changeToV7(ClassBuilder builder,ClassElement element) {
        switch (element) {
            case ClassFileVersion cfv when cfv.majorVersion() < ClassFile.JAVA_7_VERSION -> {
                builder.with(ClassFileVersion.of(ClassFile.JAVA_7_VERSION, 0));
            }
            default -> builder.with(element);
        }
    }
    
}

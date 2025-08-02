package com.github.david32768.jynxfree.classfile;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;

public class Transforms {

    private Transforms(){}

    public static byte[] addStackMap(ClassFile classfile, ClassModel cm) {
        ClassTransform ct = ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL);
        byte[] bytes = classfile.withOptions(
                    ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES,
                    ClassFile.StackMapsOption.GENERATE_STACK_MAPS,
        // PATCH_DEAD_CODE is the default but as it alters the bytecode it is deliberately added
                    ClassFile.DeadCodeOption.PATCH_DEAD_CODE)
                .transformClass(cm, ct);
        return bytes;
    }

    
}

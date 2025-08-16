package com.github.david32768.jynxfree.transform;

import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.util.List;
import java.util.Optional;

public class ClassModels {

    private ClassModels() {}
    
    public static Optional<Opcode> findDiscontinuedOpcode(ClassModel cm) {
        return cm.methods().stream()
                .flatMap(mm -> findDiscontinuedOpcode(mm).stream())
                .findAny();
    }

    public static List<MethodModel> findDiscontinuedMethods(ClassModel cm) {
        return cm.methods().stream()
                .filter(mm -> findDiscontinuedOpcode(mm).isPresent())
                .toList();
    }
    
    public static Optional<Opcode> findDiscontinuedOpcode(MethodModel mm) {
        return mm.code().stream()
                .flatMap(cd -> cd.elementStream())
                .filter(i -> i instanceof DiscontinuedInstruction)
                .map(i -> ((DiscontinuedInstruction)i).opcode())
                .findAny();
    }
    
    private static final AttributeMapper<StackMapTableAttribute> STACK_MAPPER = Attributes.stackMapTable();
    
    public static boolean hasStackMap(ClassModel cm) {
        return cm.methods().stream()
                .flatMap(m -> m.code().stream())
                .anyMatch(c -> c.findAttribute(STACK_MAPPER).isPresent());
    }
    
}

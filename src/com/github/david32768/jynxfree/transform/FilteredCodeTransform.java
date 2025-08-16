package com.github.david32768.jynxfree.transform;

import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.List;

public class FilteredCodeTransform {

    private final Predicate<MethodModel> methodFilter; 
    
    private MethodModel method;

    private FilteredCodeTransform(Predicate<MethodModel> methodFilter) {
        this.methodFilter = methodFilter;
    }
    
    public static ClassTransform classTransformOf(List<Function<MethodModel, CodeTransform>> transformers) {
         return classTransformOf(mm -> true, transformers);
    }
    
    public static ClassTransform classTransformOf(Predicate<MethodModel> methodFilter,
            List<Function<MethodModel, CodeTransform>> transformers) {
        var filterct = new FilteredCodeTransform(methodFilter);
        CodeTransform ct = CodeTransform.ACCEPT_ALL;
        for (var ctfn : transformers) {
            ct = ct.andThen(CodeTransform.ofStateful(() -> filterct.supply(ctfn)));
        }
        return ClassTransform.transformingMethodBodies(filterct::filter, ct);        
    }
    
    public static Function<MethodModel, CodeTransform> supplierOf(CodeTransform ct) {
        return mm -> ct;
    }
    
    private boolean filter(MethodModel mm) {
        method = mm;
        return methodFilter.test(mm);
    }
    
    private CodeTransform supply(Function<MethodModel, CodeTransform> fn) {
        return fn.apply(method);
    }
}

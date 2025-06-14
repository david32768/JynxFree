package com.github.david32768.jynxfree.jynx;

import java.util.function.BiConsumer;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.Message.M295;

public enum Style {
    CLASS_NAME(StyleChecker::checkClassStyle),
    PACKAGE_NAME(StyleChecker::checkPackageStyle),
    ARRAY_DESC(StyleChecker::checkTypeStyle),
    FIELD_NAME(StyleChecker::checkFieldNameStyle),
    METHOD_NAME(StyleChecker::checkMethodNameStyle),
    DESC(StyleChecker::checkDescStyle),
    CLASS_SIGNATURE(StyleChecker::checkClassSignature),
    METHOD_SIGNATURE(StyleChecker::checkMethodSignature),
    FIELD_SIGNATURE(StyleChecker::checkFieldSignature),
    ;
    
    private final BiConsumer<StyleChecker,String> validfn;
    
    private Style(BiConsumer<StyleChecker,String> validfn) {
        this.validfn = validfn;
    }
    
    public void check(StyleChecker checker, String str) {
        try {
            validfn.accept(checker, str);
        } catch(IllegalArgumentException ex) {
            LOG(M295,this,ex.getMessage()); // "%s is invalid: %s"
        }
    }
    
}

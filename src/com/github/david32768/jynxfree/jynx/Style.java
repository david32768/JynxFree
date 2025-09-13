package com.github.david32768.jynxfree.jynx;

import java.util.function.Consumer;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.my.Message.M295;

public enum Style {
    CLASS_NAME(JynxStyleChecker::checkClassStyle),
    PACKAGE_NAME(JynxStyleChecker::checkPackageStyle),
    ARRAY_DESC(JynxStyleChecker::checkArrayStyle),
    FIELD_NAME(JynxStyleChecker::checkFieldNameStyle),
    METHOD_NAME(JynxStyleChecker::checkMethodNameStyle),
    DESC(JynxStyleChecker::checkDescStyle),
    CLASS_SIGNATURE(JynxStyleChecker::checkClassSignature),
    METHOD_SIGNATURE(JynxStyleChecker::checkMethodSignature),
    FIELD_SIGNATURE(JynxStyleChecker::checkFieldSignature),
    ;
    
    private final Consumer<String> validfn;
    
    private Style(Consumer<String> validfn) {
        this.validfn = validfn;
    }
    
    public void check(String str) {
        try {
            validfn.accept(str);
        } catch(IllegalArgumentException ex) {
            LOG(ex);
            LOG(M295,this,ex.getMessage()); // "%s is invalid: %s"
        }
    }
    
}

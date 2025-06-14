package com.github.david32768.jynxfree.jynx;

public interface StyleChecker {

    void checkClassSignature(String str);

    void checkClassStyle(String str);

    void checkDescStyle(String str);

    void checkFieldNameStyle(String str);

    void checkFieldSignature(String str);

    void checkJavaMethodNameStyle(String str);

    void checkMethodNameStyle(String str);

    void checkMethodSignature(String str);

    void checkNotJavaReserved(String str);

    void checkPackageStyle(String str);

    void checkTypeStyle(String str);
    
}

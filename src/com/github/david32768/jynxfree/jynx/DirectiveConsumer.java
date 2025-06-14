package com.github.david32768.jynxfree.jynx;

public interface DirectiveConsumer {

    void endMethod(Directive dir);
    void setCode(Directive dir);

    void endClass(Directive dir);
    void endHeader(Directive dir);
    void endComponent(Directive dir);
    void endField(Directive dir);
    void endModule(Directive dir);
    void defaultVersion(Directive dir); 

    void setMethod(Directive dir);
    void setStart(Directive dir);

    void setClass(Directive dir);
    void setComponent(Directive dir);
    void setField(Directive dir);
    void setModule(Directive dir);
    void setCommon(Directive dir);
    void setHeader(Directive dir);

}

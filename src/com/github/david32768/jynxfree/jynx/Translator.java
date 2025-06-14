package com.github.david32768.jynxfree.jynx;

import java.util.Map;

public interface Translator {

    void addOwnerTranslations(Map<String, String> add);

    void addParmTranslations(Map<String, String> add);

    String translateDesc(String classname, String str);

    String translateOwner(String classname, String str);

    String translateParms(String classname, String parms);

    String translateType(String classname, String parm, boolean semi);
    
}

package com.github.david32768.jynxfree.jynx;

import java.lang.classfile.ClassSignature;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.Global.SUPPORTS;
import static com.github.david32768.jynxfree.jynx.Style.FIELD_NAME;
import static com.github.david32768.jynxfree.jynx.Style.METHOD_NAME;

import static com.github.david32768.jynxfree.my.Message.M158;
import static com.github.david32768.jynxfree.my.Message.M236;
import static com.github.david32768.jynxfree.my.Message.M258;
import static com.github.david32768.jynxfree.my.Message.M344;
import static com.github.david32768.jynxfree.my.Message.M401;
import static com.github.david32768.jynxfree.my.Message.M93;

import com.github.david32768.jynxfree.jvm.JavaReserved;

public class JynxStyleChecker {

    private JynxStyleChecker() {}
    
    public static void checkNotJavaReserved(String str) {
        Optional<JavaReserved> javaid = JavaReserved.of(str);
        if (javaid.isPresent()) {
            JavaReserved jres = javaid.get();
            if (jres.isContextual()) {
                LOG(M401,str); // "%s is a contextual reserved word"
            } else if (SUPPORTS(jres.feature())) {
                LOG(M258,str); // "%s is a reserved word and cannot be a Java Id"
            }
        }
    }
    
    public static void checkClassStyle(String str) {
        int index = str.lastIndexOf('/');
        if (index >= 0) {
            checkPackageStyle(str.substring(0,index));
        }
        String klass = str.substring(index + 1);
        checkNotJavaReserved(klass);
        if (Global.OPTION(GlobalOption.WARN_STYLE)) {
            int ch = klass.codePointAt(0);
            if (!Character.isUpperCase(ch)) {
                String classname = str.substring(index + 1);
                LOG(M93,classname); // "class name (%s) does not start with uppercase letter"
            }
        }
    }
    
    private static boolean packageChar(int codepoint) {
        return Character.isLowerCase(codepoint)
                || Character.isDigit(codepoint)
                || codepoint == '/';
    }
    
    public static void checkPackageStyle(String str) {
        String[] components = str.split("/");
        for (String component:components) {
            checkNotJavaReserved(component);
        }
        if (NameDesc.isJava(str) || Global.OPTION(GlobalOption.WARN_STYLE)) {
            if (!str.codePoints().allMatch(JynxStyleChecker::packageChar)) {
                LOG(M158,str); // "components of package %s are not all lowercase"
            }
        }
    }

    public static void checkJavaMethodNameStyle(String str) {
        int first = str.codePointAt(0);
        if (Character.isUpperCase(first) && !str.equalsIgnoreCase(str)) {
            LOG(M236,METHOD_NAME,str); // "%s (%s) starts with uppercase letter and is not all uppercase"
        }
    }
    
    public static void checkMethodNameStyle(String str) {
        checkNotJavaReserved(str);
        if (Global.OPTION(GlobalOption.WARN_STYLE)) {
            checkJavaMethodNameStyle(str);
        }
    }
    
    public static void checkFieldNameStyle(String str) {
        checkNotJavaReserved(str);
        if (Global.OPTION(GlobalOption.WARN_STYLE)) {
            int first = str.codePointAt(0);
            if (Character.isUpperCase(first) && !str.equalsIgnoreCase(str)) {
                // "%s (%s) starts with uppercase letter and is not all uppercase"
                LOG(M236,FIELD_NAME,str);
            }
        }
    }

    public static void checkArrayStyle(String str) {
        var desc = ClassDesc.ofDescriptor(str);
        if (!desc.isArray()) {
            // "%s is not an array type"
            throw new LogIllegalArgumentException(M344, str);
        }
        checkClassDesc(desc);
    }
    
    public static void checkDescStyle(String str) {
        var mnd = MethodTypeDesc.ofDescriptor(str);
        var list = mnd.parameterList();
        var rt = mnd.returnType();
        for (var parm : list) {
            checkClassDesc(parm);
        }
        checkClassDesc(rt);
    }
    
    private static void checkClassDesc(ClassDesc desc) {
        desc = desc.isArray()? desc.componentType(): desc;
        if (desc.isClassOrInterface()) {
            checkClassStyle(desc.descriptorString());
        }
    }

    public static void checkClassSignature(String str) {
        ClassSignature.parseFrom(str);
    }

    public static void checkMethodSignature(String str) {
        MethodSignature.parseFrom(str);
    }

    public static void checkFieldSignature(String str) {
        Signature.parseFrom(str);
    }
}

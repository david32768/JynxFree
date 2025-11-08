package com.github.david32768.jynxfree.jynx;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jvm.AccessFlag.*;
import static com.github.david32768.jynxfree.jynx.Directive.*;
import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.NameDesc.CLASS_NAME;
import static com.github.david32768.jynxfree.my.Message.M186;
import static com.github.david32768.jynxfree.my.Message.M283;
import static com.github.david32768.jynxfree.my.Message.M327;

import com.github.david32768.jynxfree.jvm.AccessFlag;
import com.github.david32768.jynxfree.jvm.Constants;
import com.github.david32768.jynxfree.jvm.Feature;
import com.github.david32768.jynxfree.jvm.JvmVersion;

public enum ClassType {
    
    // default super class, mandated super, feature, directive, inner directive, determinator, must_have
    
            // ANNOTATION must come before INTERFACE
    ANNOTATION_CLASS(Constants.OBJECT_CLASS, true, Feature.annotations,
            dir_define_annotation, dir_inner_define_annotation,
            EnumSet.of(acc_annotation),
            EnumSet.of(acc_annotation, acc_interface, acc_abstract)),
            // INTERFACE must be after ANNOTATION
    INTERFACE(Constants.OBJECT_CLASS, false, Feature.unlimited,
            dir_interface, dir_inner_interface,
            EnumSet.of(acc_interface),
            EnumSet.of(acc_interface, acc_abstract)),
            // PACKAGE must be after INTERFACE
    PACKAGE(Constants.OBJECT_CLASS, true, Feature.annotations,
            dir_package, null,
            null,
            EnumSet.of(acc_interface, acc_abstract, acc_synthetic)),
    ENUM(Constants.ENUM_SUPER, false, Feature.enums,
            dir_enum, dir_inner_enum,
            EnumSet.of(acc_enum),
            EnumSet.of(acc_enum, acc_super, acc_identity)),
    MODULE_CLASS(null, true, Feature.modules,
            dir_define_module, null,
            EnumSet.of(acc_module),
            EnumSet.of(acc_module)),
    RECORD(Constants.RECORD_SUPER, true, Feature.record,
            dir_record, dir_inner_record,
            EnumSet.of(acc_record, acc_identity),
            EnumSet.of(acc_record, acc_super, acc_identity)),
            // VALUE_RECORD must be after RECORD
    VALUE_RECORD(Constants.RECORD_SUPER, true, Feature.value,
            dir_value_record, dir_inner_value_record,
            EnumSet.of(acc_record, acc_value),
            EnumSet.of(acc_record, acc_value)),
    IDENTITY_CLASS(Constants.OBJECT_CLASS, false, Feature.unlimited,
            dir_class, dir_inner_class,
            EnumSet.of(acc_super, acc_identity),
            EnumSet.of(acc_super, acc_identity)),
    VALUE_CLASS(Constants.OBJECT_CLASS, false,  Feature.value,
            dir_value_class, dir_inner_value_class,
            EnumSet.of(acc_value),
            EnumSet.of(acc_value)),
    ;

    private final String defaultSuper;
    private final boolean mandatedSuper;
    private final Feature feature;
    private final Directive dir;
    private final Directive innerDir;
    private final EnumSet<AccessFlag> determinator;
    private final EnumSet<AccessFlag> must;

    private ClassType(Constants defaultSuper, boolean mandated, Feature feature,
            Directive dir, Directive innerDir,
            EnumSet<AccessFlag> determinator,
            EnumSet<AccessFlag> must) {
        assert determinator == null || must.isEmpty() || must.containsAll(determinator);
        this.defaultSuper = defaultSuper == null? null: defaultSuper.stringValue();
        this.mandatedSuper = mandated;
        this.feature = feature;
        this.dir = dir;
        this.innerDir = innerDir;
        this.determinator = determinator;
        this.must = must;
    }

    public String defaultSuper() {
        if (defaultSuper != null) {
            LOG(M327, Directive.dir_super, defaultSuper); // "added: %s %s"
        }
        return defaultSuper;
    }
    
    public static ClassType from(EnumSet<AccessFlag> accflags, JvmVersion jvmversion) {
        var classtype = Stream.of(values())
                .filter(ct -> jvmversion.supports(ct.feature))
                .filter(ct-> ct.isMe(accflags, jvmversion))
                .findFirst()
                .orElse(IDENTITY_CLASS);
        return classtype;
    }

    public static ClassType ofDir(Directive dir) {
        var classtype =  Stream.of(values())
                .filter(ct->ct.dir == dir)
                .findFirst()
                .orElseThrow();
        return classtype;
    }
    
    public static ClassType ofInnerDir(Directive innerdir) {
        var classtype =  Stream.of(values())
                .filter(ct->ct.innerDir == innerdir)
                .findFirst()
                .orElseThrow();
        return classtype;
    }
    
    public Directive getDir() {
        return dir;
    }

    public Directive getInnerDir() {
        assert Objects.nonNull(innerDir);
        return innerDir;
    }

    public EnumSet<AccessFlag> getMustHave4Class(JvmVersion jvmversion) {
        return getMustHave(jvmversion, false);
    }

    public EnumSet<AccessFlag> getMustHave4Inner(JvmVersion jvmversion) {
        return getMustHave(jvmversion, true);
    }

    private EnumSet<AccessFlag> getMustHave(JvmVersion jvmversion, boolean inner) {
        return must.stream()
                .filter(flag->jvmversion.supports(flag))
                .filter(flag->!(inner && flag == acc_super))
                .collect(()->EnumSet.noneOf(AccessFlag.class),EnumSet::add,EnumSet::addAll);
    }

    private boolean isMe(EnumSet<AccessFlag> accflags, JvmVersion jvmversion) {
        return determinator != null 
                && accflags.containsAll(forVersion(determinator, jvmversion));
    }
    
    private static EnumSet<AccessFlag> forVersion(EnumSet<AccessFlag> flags, JvmVersion jvmversion) {
        return flags.stream()
                .filter(flag->jvmversion.supports(flag))
                .collect(()->EnumSet.noneOf(AccessFlag.class),EnumSet::add,EnumSet::addAll);                
    }
    
    private final static Set<String> SUPERS = Stream.of(values())
                .map(ct -> ct.defaultSuper)
                .filter(s -> s != null)
                .collect(Collectors.toSet());
    
    public String checkSuper(String csuper) {
        CLASS_NAME.validate(csuper);
        if (Objects.equals(csuper, defaultSuper)) {
            return csuper;
        }
        if (mandatedSuper) {
            // "%s for %s must be %s"
            LOG(M186, Directive.dir_super, this, defaultSuper);
            return defaultSuper;
        }
        if (SUPERS.contains(csuper)) {
            // "%s for %s cannot be %s"
            LOG(M283, Directive.dir_super, this, csuper);
            return defaultSuper;
        }
        return csuper;
    }

}

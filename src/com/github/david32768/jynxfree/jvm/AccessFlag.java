package com.github.david32768.jynxfree.jvm;

import static java.lang.classfile.ClassFile.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jvm.Context.*;
import static com.github.david32768.jynxfree.jynx.Directive.*;
import static com.github.david32768.jynxfree.my.Message.*;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.Global.OPTION;
import static com.github.david32768.jynxfree.jynx.GlobalOption.VALHALLA;

import com.github.david32768.jynxfree.jynx.Directive;
import com.github.david32768.jynxfree.jynx.Global;
import com.github.david32768.jynxfree.jynx.GlobalOption;

// Class - Table 4.1B
// Field - Table 4.5A
// Method - Table 4.6A
// nested class - Table 4.7.6A
public enum AccessFlag implements JvmVersioned {
        acc_public(ACC_PUBLIC,0x0001,
                EnumSet.of(CLASS,INNER_CLASS,FIELD,METHOD,INIT_METHOD),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation,
                        dir_interface, dir_inner_interface,
                        dir_package,
                        dir_enum, dir_inner_enum,
                        dir_record, dir_inner_record,
                        dir_class, dir_inner_class,
                        dir_field, dir_method)),
        acc_private(ACC_PRIVATE, 0x0002,
                EnumSet.of(INNER_CLASS,FIELD,METHOD,INIT_METHOD),
                EnumSet.of(dir_inner_define_annotation,
                        dir_inner_interface,
                        dir_inner_enum,
                        dir_inner_record,
                        dir_inner_class,
                        dir_field, dir_method)),
        acc_protected(ACC_PROTECTED,0x0004,
                EnumSet.of(INNER_CLASS,FIELD,METHOD,INIT_METHOD),
                EnumSet.of(dir_inner_define_annotation,
                        dir_inner_interface,
                        dir_inner_enum,
                        dir_inner_record,
                        dir_inner_class,
                        dir_field, dir_method)),
        acc_static(ACC_STATIC,0x0008,
                EnumSet.of(INNER_CLASS,FIELD,METHOD),
                EnumSet.of(dir_inner_define_annotation,
                        dir_inner_interface,
                        dir_inner_enum,
                        dir_inner_record,
                        dir_inner_class,
                        dir_field, dir_method)),
        acc_final(ACC_FINAL,0x0010,
                EnumSet.of(CLASS,INNER_CLASS,FIELD,METHOD,PARAMETER),
                EnumSet.of(dir_enum, dir_inner_enum,
                        dir_record, dir_inner_record,
                        dir_class, dir_inner_class,
                        dir_field, dir_method,dir_parameter)),
        acc_super(Feature.superflag,ACC_SUPER,0x0020,
                EnumSet.of(CLASS),
                EnumSet.of(dir_class, dir_enum, dir_record)),
        acc_synchronized(ACC_SYNCHRONIZED,0x0020,
                EnumSet.of(METHOD),
                EnumSet.of(dir_method)),
        acc_open(Feature.modules,ACC_OPEN,0x0020,
                EnumSet.of(MODULE),
                EnumSet.of(dir_define_module)),
        acc_transitive(Feature.modules,ACC_TRANSITIVE,0x0020,
                EnumSet.of(REQUIRE),
                EnumSet.of(dir_requires)),
        acc_volatile(ACC_VOLATILE,0x0040,
                EnumSet.of(FIELD),
                EnumSet.of(dir_field)),
        acc_bridge(Feature.bridge,ACC_BRIDGE,0x0040,
                EnumSet.of(METHOD),
                EnumSet.of(dir_method)),
        acc_static_phase(Feature.modules,ACC_STATIC_PHASE,0x0040,
                EnumSet.of(REQUIRE),
                EnumSet.of(dir_requires)),
        acc_transient(ACC_TRANSIENT,0x0080,
                EnumSet.of(FIELD),
                EnumSet.of(dir_field)),
        acc_varargs(Feature.varargs,ACC_VARARGS,0x0080,
                EnumSet.of(METHOD,INIT_METHOD),
                EnumSet.of(dir_method)),
        acc_native(ACC_NATIVE,0x0100,
                EnumSet.of(METHOD),
                EnumSet.of(dir_method)),
        acc_interface(ACC_INTERFACE, 0x0200,
                EnumSet.of(CLASS,INNER_CLASS),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation,
                        dir_interface, dir_inner_interface,
                        dir_package)),
        acc_abstract(ACC_ABSTRACT,0x0400,
                EnumSet.of(CLASS,INNER_CLASS,METHOD),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation,
                        dir_interface, dir_inner_interface,
                        dir_package,
                        dir_enum, dir_inner_enum,
                        dir_record, dir_inner_record,
                        dir_class, dir_inner_class,
                        dir_method)),
        acc_strict(Feature.fpstrict,ACC_STRICT,0x0800,
                EnumSet.of(METHOD,INIT_METHOD),
                EnumSet.of(dir_method)),
        acc_synthetic(Feature.synthetic,ACC_SYNTHETIC,0x1000, // generated by ASM if synthetiic attribute present
                EnumSet.of(CLASS,INNER_CLASS,FIELD,METHOD,INIT_METHOD,PARAMETER,EXPORT,OPEN,REQUIRE),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation,
                        dir_interface, dir_inner_interface,
                        dir_package, dir_define_module,
                        dir_enum, dir_inner_enum,
                        dir_record, dir_inner_record,
                        dir_class, dir_inner_class,
                        dir_field,dir_method,dir_parameter,dir_exports,dir_opens,dir_requires)),
        acc_annotation(Feature.annotations,ACC_ANNOTATION,0x2000,
                EnumSet.of(CLASS,INNER_CLASS),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation)),
        acc_enum(Feature.enums,ACC_ENUM, 0x4000,
                EnumSet.of(CLASS,INNER_CLASS,FIELD),
                EnumSet.of(dir_enum, dir_inner_enum, dir_field)),
        acc_module(Feature.modules,ACC_MODULE,0x8000,
                EnumSet.of(CLASS),
                EnumSet.of(dir_define_module)),
        acc_mandated(Feature.mandated,ACC_MANDATED,0x8000,
                EnumSet.of(MODULE,PARAMETER,EXPORT,OPEN,REQUIRE),
                EnumSet.of(dir_define_module,dir_parameter,dir_exports,dir_opens,dir_requires)),

        valhalla_acc_strict(Feature.value, 0x800, 0x800,
                EnumSet.of(FIELD),
                EnumSet.of(dir_field)),
        valhalla_acc_identity(Feature.valhalla,ACC_SUPER,0x0020,
                EnumSet.of(INNER_CLASS),
                EnumSet.of(dir_inner_class, dir_inner_record)),
    // ASM specific pseudo access flags - not written to class file - used if appropriate attribute present
        // acc_synthetic before V1_5
        acc_record(Feature.record, 0x10000, 0x10000,
                EnumSet.of(CLASS,INNER_CLASS),
                EnumSet.of(dir_record, dir_inner_record)),
        acc_deprecated(Feature.deprecated, 0x20000, 0x20000,
                EnumSet.of(CLASS,INNER_CLASS, FIELD, METHOD, INIT_METHOD),
                EnumSet.of(dir_define_annotation, dir_inner_define_annotation,
                        dir_interface, dir_inner_interface,
                        dir_package,
                        dir_enum, dir_inner_enum,
                        dir_record, dir_inner_record,
                        dir_class, dir_inner_class,
                        dir_field, dir_method)),
    // Jynx specific pseudo access flags 0x0
        valhalla_acc_value(Feature.value, 0x0, 0x0,
                EnumSet.of(CLASS,INNER_CLASS),
                EnumSet.of(dir_class,dir_inner_class,
                        dir_record, dir_inner_record)),
        // flag for internal use - xxx_ prefix and 0x0
        xxx_component(Feature.record, 0x0,0x0,
                EnumSet.of(FIELD,METHOD),
                EnumSet.of(dir_field,dir_method)),
    ;

    private final Feature feature;
    private final int access_flag;
    private final EnumSet<Context> where;
    private final EnumSet<Directive> dirs;
    
    private AccessFlag(int access, int hex, EnumSet<Context> states, EnumSet<Directive> dirs) {
        this(Feature.unlimited,access, hex, states, dirs);
    }

    private AccessFlag(Feature feature, int access, int hex, EnumSet<Context> states, EnumSet<Directive> dirs) {
        // "%s: jynx value (%d) does not agree with classfile value(%d)"
        assert access == hex:M161.format(name(), hex, access);
        this.access_flag = access;
        this.where = states;
        this.feature = feature;
        this.dirs = dirs;
    }

    public int getAccessFlag() {
        return access_flag;
    }

    public Feature feature() {
        return feature;
    }

    
    @Override
    public JvmVersionRange range() {
        return feature.range();
    }

    public boolean isValid(Context state) {
        return where.contains(state);
    }

    public boolean isValid(Context state, Directive dir) {
        return where.contains(state) && dirs.contains(dir);
    }

    public String stringValue() {
        int index = name().lastIndexOf('_');
        return name().substring(index + 1);
    }
    
    private boolean requiresValhalla() {
        return name().contains("valhalla");
    }
    
    private boolean relevent() {
        return !requiresValhalla() || Global.OPTION(GlobalOption.VALHALLA);
    }
    
    @Override
    public String toString() {
        return stringValue();
    }

    public static Stream<AccessFlag> stream() {
        return Stream.of(values())
                .filter(AccessFlag::relevent);
    }
    
    public static Optional<AccessFlag> fromString(String token) {
        String tokenx = token.equals("fpstrict")? acc_strict.toString(): token;
        return stream()
                .filter(acc->acc.stringValue().equals(tokenx))
                .findFirst();
    }

    private boolean isPresent(int access,Context state) {
        return isValid(state) && (this.access_flag & access) != 0;
    }

    private boolean isPresent(int access,Context state, JvmVersion jvmversion) {
        return isPresent(access,state) && jvmversion.supports(this);
    }

    // basic check and disambiguate
    public static EnumSet<AccessFlag> getEnumSet(int access, Context acctype, JvmVersion jvmversion) {
        assert acctype.usesAccessFlags(): "" + acctype.toString();
        var flags = valid(access, acctype, jvmversion);
        int invalid = access &~ getAccess(flags);
        if (invalid != 0) {
            var posflags = known(invalid, acctype);
            int unknown = invalid &~ getAccess(posflags);
            if (!posflags.isEmpty()) {
                // "access flag(s) %s in context %s not valid for version %s"
                LOG(M110, posflags, acctype, jvmversion);
            }
            if (unknown != 0) {
                // "unknown access flag (%#04x) in context %s ignored"
                LOG(M107, unknown, acctype);
            }
        }
        return flags;
    }

    public static EnumSet<AccessFlag> valid(int access, Context context, JvmVersion jvmversion) {
        assert context.usesAccessFlags(): "" + context.toString();
        var flagset = stream()
                .filter(flag->flag.isPresent(access, context, jvmversion))
                .collect(()->EnumSet.noneOf(AccessFlag.class), EnumSet::add, EnumSet::addAll);
        if (OPTION(VALHALLA) && (context == CLASS || context == INNER_CLASS)) {
            if (flagset.contains(acc_super) || flagset.contains(valhalla_acc_identity)) {
            } else {
                flagset.add(valhalla_acc_value);
            }
        }
        return flagset;
    }

    public static EnumSet<AccessFlag> known(int access, Context acctype) {
        assert acctype.usesAccessFlags(): "" + acctype.toString();
        return stream()
                    .filter(flag->flag.isPresent(access, acctype))
                    .collect(()->EnumSet.noneOf(AccessFlag.class), EnumSet::add, EnumSet::addAll);
    }

    public static String[] stringArrayOf(EnumSet<AccessFlag> flags) {
        return flags.stream()
                .map(AccessFlag::stringValue)
                .toArray(String[]::new);
    }

    public static int getAccess(EnumSet<AccessFlag> flags) {
        return flags.stream()
                .mapToInt(AccessFlag::getAccessFlag)
                .reduce(0, (accumulator, _item) -> accumulator | _item);
    }

}

package com.github.david32768.jynxfree.jynx;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import static com.github.david32768.jynxfree.my.Message.M218;
import static com.github.david32768.jynxfree.my.Message.M219;
import static com.github.david32768.jynxfree.my.Message.M32;
import static com.github.david32768.jynxfree.my.Message.M4;
import static com.github.david32768.jynxfree.my.Message.M73;
import static com.github.david32768.jynxfree.my.Message.M999;

import com.github.david32768.jynxfree.jvm.ConstantPoolType;
import com.github.david32768.jynxfree.jvm.JvmVersion;
import com.github.david32768.jynxfree.jvm.JvmVersioned;

public class Global {

    private final Logger logger;
    private final EnumSet<GlobalOption> options;
    private JvmVersion jvmVersion;
    private String classname;
    private final MainOption main;
    
    private Translator ttanslator;
    private StyleChecker styleChecker;
    
    private Global() {
        this.options = EnumSet.of(GlobalOption.DEBUG);
        this.logger  = new Logger("");
        this.jvmVersion = null;
        this.classname = null;
        this.main = null;
        this.styleChecker = null;
    }

    private Global(MainOption type, EnumSet<GlobalOption> options) {
        this.main = type;
        this.options = options;
        this.logger  = new Logger(type.name().toLowerCase());
        this.jvmVersion = null;
    }
    
    private static Global global = new Global();
    
    public static void newGlobal(MainOption type) {
        global = new Global(type, EnumSet.noneOf(GlobalOption.class));
        // "%nJynx %s %s; Java runtime version %s"
        LOG(M4, type.name(), type.version(), Runtime.version());
    }
    
    public static Logger LOGGER() {
        return global.logger;
    }

    public static void setJvmVersion(JvmVersion jvmversion) {
        assert global.jvmVersion == null || global.jvmVersion == jvmversion;
        global.jvmVersion = jvmversion;
    }
    
    public static void setStyleChecker(StyleChecker checker) {
        assert global.styleChecker == null;
        global.styleChecker = checker;
    }
    
    public static void setTranslator(Translator translator) {
        assert global.ttanslator == null;
        global.ttanslator = translator;
    }
    
    public static void setClassName(String classname) {
        assert global.classname == null;
        global.classname = classname;
    }
    
    public static JvmVersion JVM_VERSION() {
        Objects.nonNull(global.jvmVersion);
        return global.jvmVersion;
    }
    
    public static String CLASS_NAME() {
        Objects.nonNull(global.classname);
        return global.classname;
    }

    public static boolean CHECK_STYLE(Style style, String str) {
        if (style != null && global.styleChecker != null) {
            style.check(global.styleChecker, str);
        }
        return true;
    }
    
    public static boolean CHECK_SUPPORTS(JvmVersioned feature) {
        if (feature != null && global.jvmVersion != null) {
            return global.jvmVersion.checkSupports(feature);
        }
        return true;
    }
    
    public static boolean CHECK_CAN_LOAD(ConstantPoolType cp) {
        if (global.jvmVersion != null) {
            return cp.checkLoadableBy(global.jvmVersion);
        }
        return true;
    }
    
    public static boolean SUPPORTS(JvmVersioned feature) {
        if (feature != null && global.jvmVersion != null) {
            return global.jvmVersion.supports(feature);
        }
        return feature == null;
    }
    
    public static boolean ADD_OPTION(GlobalOption option) {
        if (global.main.usesOption(option)) {
            return global.options.add(option);
        } else {
            LOG(M73,option); // "irrelevant option %s ignored"
            return false;
        }
    }
    
    public static void ADD_OPTIONS(EnumSet<GlobalOption> optionset) {
        optionset.stream()
                .forEach(Global::ADD_OPTION);
    }
    
    public static boolean OPTION(GlobalOption option) {
        return global.options.contains(option);
    }
    
    public static EnumSet<GlobalOption> OPTIONS() {
        return global.options.clone();
    }
    
    public static Optional<String> setOptions(String[] args) {
        int i = 0;
        String[] remainder = new String[0];
        for (; i < args.length; ++i) {
            String argi = args[i];
            if (argi.isEmpty()) {
                continue;
            }
            if (GlobalOption.mayBeOption(argi)) {
                Optional<GlobalOption> opt = GlobalOption.optArgInstance(argi);
                if (opt.isPresent()) {
                    GlobalOption option = opt.get();
                    ADD_OPTION(option);
                } else {
                    LOG(M32,argi); // "%s is not a valid option"
                }
            } else {
                remainder = Arrays.copyOfRange(args, i, args.length);
                if (remainder.length == 1) {
                    return Optional.of(args[i]);
                }
                break;
            }
        }
        if (remainder.length == 0) {
            LOG(M218); //"SYSIN will be used as input"
        } else {
            LOG(M219,Arrays.asList(remainder)); // "wrong number of parameters after options %s"
        }
        return Optional.empty();
    }

    public static void LOG(JynxMessage msg,Object... objs) {
        global.logger.log(msg,objs);
    }

    public static void LOG(String linestr, JynxMessage msg, Object... objs) {
        global.logger.log(linestr, msg, objs);
    }

    public static void LOG(Throwable ex, JynxMessage msg, Object... objs) {
        if (OPTION(GlobalOption.DEBUG)) {
            ex.printStackTrace();;
        }
        global.logger.log(msg, objs);
    }

    public static void LOG(Throwable ex) {
        if (OPTION(GlobalOption.DEBUG)) {
            ex.printStackTrace();;
        }
        if (ex instanceof LogIllegalArgumentException) {
            return; // already logged
        }
        if (ex instanceof LogIllegalStateException) {
            return; // already logged
        }
        if (ex instanceof SevereError) {
            return; // already logged
        }
        LOG(M999,ex.toString()); // "%s"
    }

    public static boolean END_MESSAGES(String classname) {
        return global.logger.printEndInfo(classname);
    }
    
    public static MainOption MAIN_OPTION() {
        return global.main;
    }

    public static String TRANSLATE_DESC(String str) {
        return global.ttanslator.translateDesc(CLASS_NAME(),str);
    }
    
    public static String TRANSLATE_PARMS(String str) {
        return global.ttanslator.translateParms(CLASS_NAME(),str);
    }
    
    public static String TRANSLATE_TYPE(String str, boolean semi) {
        return global.ttanslator.translateType(CLASS_NAME(),str, semi);
    }
    
    public static String TRANSLATE_OWNER(String str) {
        return global.ttanslator.translateOwner(CLASS_NAME(),str);
    }
}

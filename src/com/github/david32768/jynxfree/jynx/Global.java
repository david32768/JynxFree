package com.github.david32768.jynxfree.jynx;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.david32768.jynxfree.my.Message.M32;
import static com.github.david32768.jynxfree.my.Message.M343;
import static com.github.david32768.jynxfree.my.Message.M67;
import static com.github.david32768.jynxfree.my.Message.M73;
import static com.github.david32768.jynxfree.my.Message.M74;
import static com.github.david32768.jynxfree.my.Message.M81;
import static com.github.david32768.jynxfree.my.Message.M82;
import static com.github.david32768.jynxfree.my.Message.M999;

import com.github.david32768.jynxfree.jvm.ConstantPoolType;
import com.github.david32768.jynxfree.jvm.JvmVersion;
import com.github.david32768.jynxfree.jvm.JvmVersioned;

public class Global {
    
    private final MainOption main;    
    private final Logger logger;
    private final EnumSet<GlobalOption> options;
    private final EnumSet<GlobalOption> addedOptions;
    private final Global last;
    
    private JvmVersion jvmVersion;

    private Global() {
        this.options = EnumSet.of(GlobalOption.DEBUG);
        this.addedOptions = EnumSet.noneOf(GlobalOption.class);
        this.logger  = new Logger("");
        this.jvmVersion = null;
        this.main = null;
        this.last = null;
    }

    private Global(MainOption type, EnumSet<GlobalOption> options, Global last) {
        this.main = type;
        this.options = options;
        this.addedOptions = EnumSet.noneOf(GlobalOption.class);
        this.logger  = new Logger(type.name().toLowerCase());
        this.jvmVersion = null;
        this.last = last;
    }
    
    private static Global global = new Global();
    
    public static void newGlobal(MainOption type) {
        global = new Global(type, EnumSet.noneOf(GlobalOption.class), null);
        type.printHeader();
    }
    
    public static void pushGlobal(MainOption type) {
        var options = OPTIONS();
        global = new Global(type, EnumSet.noneOf(GlobalOption.class), global);
        type.printHeader();
        ADD_RELEVENT_OPTIONS(options);
        printOptions();
    }
    
    public static void popGlobal() {
        if (global.last == null) {
            // "unable to pop global as stack empty"
            throw new LogIllegalStateException(M343);
        }
        global = global.last;
    }
    
    public static Logger LOGGER() {
        return global.logger;
    }

    public static void setJvmVersion(JvmVersion jvmversion) {
        assert global.jvmVersion == null || global.jvmVersion == jvmversion;
        global.jvmVersion = jvmversion;
    }
    
    public static JvmVersion JVM_VERSION() {
        assert Objects.nonNull(global.jvmVersion);
        return global.jvmVersion;
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
            boolean added = global.options.add(option); 
            if (added) {
                global.addedOptions.add(option);
            }
            return added;
        } else {
            LOG(M73,option); // "irrelevant option %s ignored"
            return false;
        }
    }
    
    public static void ADD_OPTIONS(EnumSet<GlobalOption> optionset) {
        optionset.stream()
                .forEach(Global::ADD_OPTION);
    }
    
    public static void ADD_RELEVENT_OPTIONS(EnumSet<GlobalOption> optionset) {
        optionset.stream()
                .filter(opt -> global.main.usesOption(opt))
                .forEach(Global::ADD_OPTION);
    }
    
    public static boolean OPTION(GlobalOption option) {
        return global.options.contains(option);
    }
    
    public static EnumSet<GlobalOption> OPTIONS() {
        return global.options.clone();
    }
    
    public static String[] setOptions(String[] args) {
        String[] parms = setOptionsImpl(args);
        global.addedOptions.clear();
        // "options are %s"
        LOG(M74, OPTIONS());
        // "parameters are %s"
        LOG(M67, Arrays.toString(parms));
        return parms;
    }

    private static String[] setOptionsImpl(String[] args) {
        int i = 0;
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
                return Arrays.copyOfRange(args, i, args.length);
            }
        }
        return new String[0];
    }

    public static void printOptions() {
        global.addedOptions.clear();
        // "options are %s"
        LOG(M81,global.options);
    }
    
    public static void printAddedOptions() {
        if (!global.addedOptions.isEmpty()) {
            // "added options are %s"
            LOG(M82,global.addedOptions);
            global.addedOptions.clear();
        }
    }
    
    public static void LOG(JynxMessage msg,Object... objs) {
        global.logger.log(msg,objs);
    }

    public static void LOG(String linestr, JynxMessage msg, Object... objs) {
        global.logger.log(linestr, msg, objs);
    }

    public static void LOG(Throwable ex, JynxMessage msg, Object... objs) {
        if (OPTION(GlobalOption.DEBUG)) {
            ex.printStackTrace();
        }
        global.logger.log(msg, objs);
    }

    public static void LOG(Throwable ex, String linestr, JynxMessage msg, Object... objs) {
        if (OPTION(GlobalOption.DEBUG)) {
            ex.printStackTrace();
        }
        global.logger.log(linestr, msg, objs);
    }

    public static void LOG(Throwable ex) {
        if (OPTION(GlobalOption.DEBUG)) {
            ex.printStackTrace();
        }
        if (ex instanceof LogIllegalArgumentException) {
            return; // already logged
        }
        if (ex instanceof LogIllegalStateException) {
            return; // already logged
        }
        if (ex instanceof LogUnsupportedOperationException) {
            return; // already logged
        }
        if (ex instanceof SevereError) {
            return; // already logged
        }
        LOG(M999, ex); // "%s"
    }

    public static boolean END_MESSAGES(String classname) {
        return global.logger.printEndInfo(classname);
    }
    
    public static boolean END_MESSAGES(List<String> parms) {
        return global.logger.printEndInfo(parms);
    }
    
    public static MainOption MAIN_OPTION() {
        return global.main;
    }

}

package com.github.david32768.jynxfree.jynx;

import java.util.function.Function;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.my.Message.*;


public enum GlobalOption {

    // information
    HELP("h", M1), // "display help message"
    VERSION("V", M2), //"display version information"

    SYSIN("", M7), // "use SYSIN as input file"
    USE_STACK_MAP(M19), // "use supplied stack map instead of ASM generated"
    WARN_UNNECESSARY_LABEL(M10), // "warn if label unreferenced or alias"
    WARN_STYLE(M15), // "warn if names non-standard"
    GENERATE_LINE_NUMBERS(M9), // "generate line numbers"
    BASIC_VERIFIER(M16), // "only use ASM BasicVerifier"
    CHECK_REFERENCES(M8), // "check that called methods or used fields exist (on class path)"
    VALIDATE_ONLY(M51), // "do not output class file"
    TRACE(M23), // "print (ASMifier) trace"
    SYMBOLIC_LOCAL(M44), // "local variables are symbolic not absolute integers"

    VERIFIER_PLATFORM(M37), // "Use ClassFile Verifier with Platform Loader only (the default is system loader)"
    
    UPGRADE_TO_V7(M11), // "upgrade version to Version V1_7"
    SKIP_CODE(M39), // "do not produce code"
    SKIP_DEBUG(M29), // "do not produce debug info"
    SKIP_FRAMES(M30), // "do not produce stack map"
    SKIP_ANNOTATIONS(M18), // "do not produce annotations"
    DOWN_CAST(M14), // "if necessary reduces JVM release to maximum supported by ASM version"
    GENERIC_SWITCH(M148), // "optimise switch instruction size"
    SKIP_STACK(M609), // "do not print stack after each instruction"
    VALHALLA(M601), // "Valhalla - limited support; may change"
    
    DEBUG(M13), // "print stack trace(s)"
    DETAIL(M17),  // "print constant pool, instructions and other detail"
    DETAIL_CONSTANT_POOL(M531), // "print constant pool detail"
    DETAIL_INSTRUCTIONS(M532), // "print instructions"
    OMIT_COMMENT(M528), // "omit start and length values"
    INCREASE_MESSAGE_SEVERITY(M25), // "treat warnings as errors etc."
    SUPPRESS_WARNINGS(M27), // "suppress warnings"
    
    // internal

    __STRUCTURED_LABELS(null), // labels are numeric level
    __WARN_INDENT(null), // "check indent for structured code"
    ;

    private final String msg;
    private final String abbrev;

    
    private GlobalOption(JynxMessage msg) {
        this(null, msg);
    }

    private GlobalOption(String abbrev, JynxMessage msg) {
        this.msg = msg == null? null: msg.format();
        this.abbrev = abbrev;
        // "abbrev '%s' for option %s has invalid name"
        assert abbrev == null || abbrev.isEmpty() || NameDesc.OPTION.isValid(abbrev):M334.format(abbrev,name());
        // "option '%s' has invalid name"
        assert msg == null && name().startsWith("__") || NameDesc.OPTION.isValid(name()):M336.format(name());
    }

    public boolean isExternal() {
        return msg != null;
    }

    private static boolean unique(Function<GlobalOption,String> strfn) {
       String[] abbrevs = Stream.of(values())
                .map(strfn)
                .filter(a -> a != null)
                .map(a -> a.replace('-', '_').toLowerCase())
                .toArray(String[]::new);
       return abbrevs.length == Stream.of(abbrevs)
               .distinct()
               .count();
    }
    
    static {
        // "GlobalOption abbreviations are not unique after transform"
        assert unique(opt->opt.abbrev): M332.format();
        // "GlobalOption names are not unique after transform"
        assert unique(opt->opt.name()): M333.format();
    }
    
    private final static String OPTION_PREFIX = "--";
    private final static String ABBREV_PREFIX = "-";

    private static boolean isEqual(String myname, String option, String prefix) {
        return myname != null && option.startsWith(prefix)
                && option
                    .substring(prefix.length())
                    .replace('-', '_')
                    .equalsIgnoreCase(myname);
    }
    
    public static boolean mayBeOption(String option) {
        return option.startsWith(ABBREV_PREFIX);
    }
    
    public boolean isArg(String option) {
        return isEqual(name(), option, OPTION_PREFIX) || isEqual(abbrev, option, ABBREV_PREFIX);
    }
    
    public String asArg() {
        return OPTION_PREFIX + name();
    }
    
    public static Optional<GlobalOption> optInstance(String str) {
        String optstr = str.startsWith(OPTION_PREFIX)?
                str:
                OPTION_PREFIX + str;
        return Stream.of(values())
                .filter(GlobalOption::isExternal)
                .filter(g -> isEqual(g.name(), optstr, OPTION_PREFIX))
                .findFirst();
    }

    public static Optional<GlobalOption> optArgInstance(String str) {
        return Stream.of(values())
                .filter(GlobalOption::isExternal)
                .filter(g -> g.isArg(str))
                .findFirst();
    }
    
    public String description() {
        return String.format("%s%s %s",OPTION_PREFIX,name(),msg);
    }
    
}

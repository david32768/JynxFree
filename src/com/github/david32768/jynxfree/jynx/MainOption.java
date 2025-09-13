package com.github.david32768.jynxfree.jynx;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jynx.GlobalOption.*;
import static com.github.david32768.jynxfree.jynx.MainConstants.*;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

import static com.github.david32768.jynxfree.my.Message.M331;
import static com.github.david32768.jynxfree.my.Message.M4;
import static com.github.david32768.jynxfree.my.Message.M6;

public enum MainOption {
    
    ASSEMBLY("jynx",
        String.format(" {options} %s_file", JX_SUFFIX),
        List.of(
            String.format("produces a class file from a %s file", JX_SUFFIX)
        ),
        EnumSet.of(SYSIN, USE_STACK_MAP, WARN_UNNECESSARY_LABEL, WARN_STYLE, 
                GENERATE_LINE_NUMBERS, BASIC_VERIFIER, VERIFIER_PLATFORM,
                CHECK_REFERENCES, VALIDATE_ONLY, TRACE, SYMBOLIC_LOCAL,
                DEBUG, INCREASE_MESSAGE_SEVERITY, SUPPRESS_WARNINGS,
                VALHALLA, GENERIC_SWITCH,
                __STRUCTURED_LABELS, __WARN_INDENT)
    ),
    COMPARE("compare",
        " {options}  class-name|class_file class-name|class_file",
        List.of(
            "checks that classes are the same according to ASM Textifier"
        ),
        EnumSet.of(USE_STACK_MAP, DEBUG)
    ),
    DISASSEMBLY("tojynx",
        String.format(" {options}  class-name|class_file > %s_file", JX_SUFFIX),
        List.of(
            String.format("produces a %s file from a class", JX_SUFFIX),
            String.format("any %s options are added to %s directive",
                    ASSEMBLY.extname.toUpperCase(), Directive.dir_version)
        ),
        EnumSet.of(SKIP_CODE, SKIP_DEBUG, SKIP_FRAMES, SKIP_ANNOTATIONS, DOWN_CAST,
                VALHALLA, SKIP_STACK, UPGRADE_TO_V7,
                DEBUG, INCREASE_MESSAGE_SEVERITY)
    ),
    ROUNDTRIP("roundtrip",
        " {options}  class-name|class_file|txt-file",
        List.of(
            String.format("checks that %s followed by %s produces an equivalent class",
                    DISASSEMBLY.extname.toUpperCase(), ASSEMBLY.extname.toUpperCase()),
            "according to ASM Textifier",
            "txt-file is a .txt file containing [ {options} [class-name|class-file] ]*"    
        ),
        EnumSet.of(USE_STACK_MAP, BASIC_VERIFIER, VERIFIER_PLATFORM,
                SKIP_FRAMES, DOWN_CAST, DEBUG, SUPPRESS_WARNINGS)
    ),
    STRUCTURE("structure",
        " {options}  class-name|class_file",
        List.of(
            "prints a skeleton of class structure"
        ),
        EnumSet.of(DETAIL, OMIT_COMMENT, DEBUG, VALHALLA)
    ),
    UPGRADE("upgrade",
        " {options}  class-name|class_file|zip-file|jar-file directory-for-upgraded-class(es)",
        List.of(
            "upgrades class to Java Version 7"
        ),
        EnumSet.of(DEBUG)
    ),
    VERIFY("verify",
        String.format(" {options}  class-name|class_file [%s-file]?", HINTS_SUFFIX),
        List.of(
            "verifies using classfile verifier",
            String.format("if present %s file only contains a Jynx %s directive",
                    HINTS_SUFFIX, Directive.dir_hints),
            "adds temporary stack map to verify pre Java V1_7 unless contains jsr,ret"
        ),
        EnumSet.of(VERIFIER_PLATFORM, DEBUG)
    ),
    ;

    private final static int JYNX_VERSION = 0;
    private final static int JYNX_RELEASE = 24;
    private final static int JYNX_BUILD = 5;

    private final int version;
    private final int release;
    private final int build;
    private final String extname;
    private final String usage;
    private final List<String> description;
    private final EnumSet<GlobalOption> options;

    private MainOption(String extname, String usage, List<String> description, EnumSet<GlobalOption> options) {
        this.extname = extname;
        this.usage = " " + extname.toLowerCase() + String.format(usage, JX_SUFFIX);
        this.description = description;
        this.options = options;
        this.version = JYNX_VERSION;
        this.release = JYNX_RELEASE;
        this.build = JYNX_BUILD;
    }

    public boolean run(String[] args) {
        var main = this.mainOptionService();
        try (PrintWriter pw = new PrintWriter(System.out)) {
            boolean success;
            try {
                success = main.call(pw, args);
            } catch(LogUnsupportedOperationException ex) {
                return false;
            } catch (Exception ex) {
                LOG(ex);
                return false;
            }
            pw.flush();
            return success;
        }
    }
    
    public String extname() {
        return extname;
    }

    public String version() {
        return String.format("%d.%d.%d", version, release, build);
    }

    public void printHeader() {
        // "%nJynx %s %s; Java runtime version %s"
        LOG(M4, name(), version(), Runtime.version());
    }
    
    public boolean usesOption(GlobalOption opt) {
        return options.contains(opt)
                || this == DISASSEMBLY && ASSEMBLY.usesOption(opt) && opt != SYSIN;
    }

    public void appUsageSummary() {
        System.err.println(usage);
        description.forEach(str -> System.err.format("   (%s)%n", str));
        System.err.println();
    }

    public void appUsage() {
        appUsageSummary();
        Global.LOG(M6); // "Options are:%n"
        for (GlobalOption opt:options) {
            if (opt.isExternal()) {
                System.err.println(" " + opt.description());            
            }
        }
        System.err.println();
    }

    public MainOptionService mainOptionService() {
        var main = MainOptionService.find(this);
        if (main.isEmpty()) {
            // "MainOption service for %s not found"
            String msg = M331.format(main);
            throw new UnsupportedOperationException(msg);            
        }
        return main.get();
    }
    
    public static Optional<MainOption> getInstance(String str) {
        return Arrays.stream(values())
                .filter(mo->mo.extname.equalsIgnoreCase(str))
                .findAny();
    }

    public static String mains() {
        return Stream.of(values())
                .map(MainOption::extname)
                .collect(Collectors.joining("|", "[", "]"));
    }
}

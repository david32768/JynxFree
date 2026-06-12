package com.github.david32768.jynxfree.jynx;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.david32768.jynxfree.jynx.GlobalOption.*;
import static com.github.david32768.jynxfree.jynx.MainConstants.*;

import static com.github.david32768.jynxfree.jynx.Global.ADD_OPTIONS;
import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.Global.LOGGER;
import static com.github.david32768.jynxfree.jynx.Global.OPTIONS;
import static com.github.david32768.jynxfree.my.Message.M298;
import static com.github.david32768.jynxfree.my.Message.M331;
import static com.github.david32768.jynxfree.my.Message.M4;
import static com.github.david32768.jynxfree.my.Message.M6;
import static com.github.david32768.jynxfree.my.Message.M997;

public enum MainOption {
    
    ASSEMBLY("jynx",
        String.format(" {options} %s_file [output_directory]?", JX_SUFFIX),
        List.of(
            String.format("produces a class file from a %s file", JX_SUFFIX)
        ),
        EnumSet.of(SYSIN, USE_STACK_MAP, WARN_UNNECESSARY_LABEL, WARN_STYLE, 
                GENERATE_LINE_NUMBERS, BASIC_VERIFIER, VERIFIER_PLATFORM,
                CHECK_REFERENCES, VALIDATE_ONLY, TRACE, SYMBOLIC_LOCAL,
                DEBUG, INCREASE_MESSAGE_SEVERITY, SUPPRESS_WARNINGS,
                GENERIC_SWITCH,
                __STRUCTURED_LABELS, __WARN_INDENT)
    ),
    COMPARE("compare",
        " {options}  class-name|class_file class-name|class_file",
        List.of(
            "checks that classes are the same according to ASM Textifier"
        ),
        EnumSet.of(DEBUG, SKIP_FRAMES, QUICK)
    ),
    DISASSEMBLY("tojynx", true,
        String.format(" {options}  class-name|class_file > %s_file", JX_SUFFIX),
        List.of(
            String.format("produces a %s file from a class", JX_SUFFIX),
            String.format("any %s options are added to %s directive",
                    ASSEMBLY.extname.toUpperCase(), Directive.dir_version)
        ),
        EnumSet.of(SKIP_CODE, SKIP_DEBUG, SKIP_FRAMES, SKIP_ANNOTATIONS, DOWN_CAST,
                SKIP_STACK, UPGRADE_TO_V7, VALHALLA, LDC_ONLY,
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
        EnumSet.of(USE_STACK_MAP, BASIC_VERIFIER, VERIFIER_PLATFORM, VALHALLA,
                SKIP_FRAMES, DOWN_CAST, DEBUG, SUPPRESS_WARNINGS, QUICK)
    ),
    STRUCTURE("structure", true,
        " {options}  class-name|class_file",
        List.of(
            "prints a skeleton of class structure"
        ),
        EnumSet.of(DETAIL, DETAIL_CONSTANT_POOL, DETAIL_INSTRUCTIONS, OMIT_COMMENT, DEBUG, VALHALLA)
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

    private final String extname;
    private final String usage;
    private final List<String> description;
    private final EnumSet<GlobalOption> options;
    private final PrintStream batchStream;

    private MainOption(String extname, String usage, List<String> description, EnumSet<GlobalOption> options) {
        this(extname, false, usage, description, options);
    }

    private MainOption(String extname, boolean batcherr, String usage, List<String> description, EnumSet<GlobalOption> options) {
        this.extname = extname;
        this.usage = " " + extname.toLowerCase() + String.format(usage, JX_SUFFIX);
        this.description = description;
        this.options = options;
        this.batchStream = batcherr? System.err: System.out;
    }

    public boolean run(String[] args) {
        try (PrintWriter pw = new PrintWriter(System.out)) {
            var main = this.mainOptionService();
            if (args.length == 1 && args[0].endsWith(TXT_SUFFIX)) {
                return runList(pw, main, args[0]);
            }
            return run(pw, main, args);
        }
    }
    
    private boolean run(PrintWriter pw, MainOptionService main, String[] args) {
        boolean success;
        try {
            success = main.call(pw, args);
        } catch(LogUnsupportedOperationException ex) {
            success = false;
        } catch (Exception ex) {
            LOG(ex);
            success = false;
        }
        pw.flush();
        return success;
    }
    
    private boolean runList(PrintWriter pw, MainOptionService main, String listfile) {
        EnumSet<GlobalOption> baseoptions = OPTIONS();
        int ct = 0;
        int okct = 0;
        int errct = 0;
        int parmct = 0;
        long start = System.currentTimeMillis();
        try {
            List<String> lines = Files.readAllLines(Paths.get(listfile));
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith(";")
                        || line.isEmpty()
                        || line.endsWith("/")
                        || line.endsWith("\\")
                        || line.endsWith(".MF")) {
                    continue;
                }
                LOGGER().setLine(line);
                var args = line.split(" ");
                Global.newGlobal(this);
                ADD_OPTIONS(baseoptions);
                String[] mainargs = Global.setOptions(args);
                if (mainargs.length == 0 && ct == 0) {
                    baseoptions = OPTIONS();
                    continue;
                }
                ++ct;
                boolean success;
                try {
                    success = run(pw, main, mainargs);
                } catch (SevereError ex) {
                    success = false;
                } catch (Exception ex) {
                    // "%s"
                    LOG(M997, ex.getMessage());
                    success = false;
                }
                if (success) {
                    ++okct;
                } else {
                    // "%s of %s failed"
                    batchStream.println(M298.format(main.main(), Arrays.toString(mainargs)));
                    ++errct;
                }
            }
        } catch (IOException ex) {
            LOG(ex);
            return false;
        }
        long end = System.currentTimeMillis();
        double minutes = (end - start)/60000.;
        batchStream.format("%nclasses = %d (ok = %d) (%.2f mins)%n", ct, okct, minutes);
        if (ct != okct) {
            batchStream.format("    %6d failed  %6d mainargs%n", errct, parmct);
        }
        batchStream.println();
        batchStream.flush();
        return errct == 0;
    }
    
    public String extname() {
        return extname;
    }

    public static String version(MainOption main) {
        if (main == null) {
            return "0.25.4";
        }
        var service = MainOptionService.find(main);
        if (service.isPresent()) {
            return main.name() + " " + service.get().version();
        }
        return main.name() + " not found";        
    }
    
    public void printHeader() {
        // "%nJynx %s; Java runtime version %s"
        LOG(M4, version(this), Runtime.version());
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

package com.github.david32768.jynxfree.jvm;

import java.lang.classfile.ClassFile;
import java.util.HashMap;
import java.util.Map;

import static com.github.david32768.jynxfree.jynx.Message.*;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.jynx.Global.OPTION;
import static com.github.david32768.jynxfree.jynx.GlobalOption.VALHALLA;

import com.github.david32768.jynxfree.jynx.Directive;

public enum JvmVersion {

    // MUST BE IN RELEASE ORDER for compareTo
    V1_0_2(45, 45, 0), // 45.0 to 45.3
    
    V1_1(ClassFile.JAVA_1_VERSION, 45, 3), // 45.3 to 45.65535

    V1_2(ClassFile.JAVA_2_VERSION, 46),
    V1_3(ClassFile.JAVA_3_VERSION, 47),
    V1_4(ClassFile.JAVA_4_VERSION, 48),
    V1_5(ClassFile.JAVA_5_VERSION, 49),
    V1_6JSR(ClassFile.JAVA_6_VERSION, 50), // may contain opcodes jsr, ret
    V1_6(ClassFile.JAVA_6_VERSION, 50),
    V1_7(ClassFile.JAVA_7_VERSION, 51),
    V1_8(ClassFile.JAVA_8_VERSION, 52),
    V9(ClassFile.JAVA_9_VERSION, 53),
    V10(ClassFile.JAVA_10_VERSION, 54),
    V11(ClassFile.JAVA_11_VERSION, 55),
    V12(ClassFile.JAVA_12_VERSION, 56),
    V12_PREVIEW(ClassFile.JAVA_12_VERSION, 56, ClassFile.PREVIEW_MINOR_VERSION),
    V13(ClassFile.JAVA_13_VERSION, 57),
    V13_PREVIEW(ClassFile.JAVA_13_VERSION, 57, ClassFile.PREVIEW_MINOR_VERSION),
    V14(ClassFile.JAVA_14_VERSION, 58),
    V14_PREVIEW(ClassFile.JAVA_14_VERSION, 58, ClassFile.PREVIEW_MINOR_VERSION),
    V15(ClassFile.JAVA_15_VERSION, 59),
    V15_PREVIEW(ClassFile.JAVA_15_VERSION, 59, ClassFile.PREVIEW_MINOR_VERSION),
    V16(ClassFile.JAVA_16_VERSION, 60),
    V16_PREVIEW(ClassFile.JAVA_16_VERSION, 60, ClassFile.PREVIEW_MINOR_VERSION),
    V17(ClassFile.JAVA_17_VERSION, 61),
    V17_PREVIEW(ClassFile.JAVA_17_VERSION, 61, ClassFile.PREVIEW_MINOR_VERSION),
    V18(ClassFile.JAVA_18_VERSION, 62),
    V18_PREVIEW(ClassFile.JAVA_18_VERSION, 62, ClassFile.PREVIEW_MINOR_VERSION),
    V19(ClassFile.JAVA_19_VERSION, 63),
    V19_PREVIEW(ClassFile.JAVA_19_VERSION, 63, ClassFile.PREVIEW_MINOR_VERSION),
    V20(ClassFile.JAVA_20_VERSION, 64),
    V20_PREVIEW(ClassFile.JAVA_20_VERSION, 64, ClassFile.PREVIEW_MINOR_VERSION),
    V21(ClassFile.JAVA_21_VERSION, 65),
    V21_PREVIEW(ClassFile.JAVA_21_VERSION, 65, ClassFile.PREVIEW_MINOR_VERSION),
    V22(ClassFile.JAVA_22_VERSION, 66),
    V22_PREVIEW(ClassFile.JAVA_22_VERSION, 66, ClassFile.PREVIEW_MINOR_VERSION),
    V23(ClassFile.JAVA_23_VERSION, 67),
    V23_PREVIEW(ClassFile.JAVA_23_VERSION, 67, ClassFile.PREVIEW_MINOR_VERSION),
    V24(ClassFile.JAVA_24_VERSION, 68),
    V24_PREVIEW(ClassFile.JAVA_24_VERSION, 68, ClassFile.PREVIEW_MINOR_VERSION),
    V25(69, 69), // Opcodes.V25
    V25_PREVIEW(69, 69, ClassFile.PREVIEW_MINOR_VERSION),
    
    NEVER(0xffff, 0xffff); // must be last
    
    private final long release; // 0x00000000_major_minor
    private final int major;
    private final int minor;

    private JvmVersion(int java, int major) {
        this(java, major, 0);
    }

    private JvmVersion(int java, int major, int minor) {
        assert java == major;
        this.major = java;
        this.minor = minor;
        this.release = Integer.toUnsignedLong((major << 16) | minor);
        // "invalid major version - %s"
        assert isUnsignedShort(major) && major > MAJOR_BASE:M20.format(this.name());
        // "invalid minor version - %s"
        assert isUnsignedShort(minor):M21.format(this.name());
        // "invalid minor version for major version (spec table 4.1A) - %s"
        assert major < MAJOR_PREVIEW || minor == 0 || minor == PREVIEW:M91.format(this.name());
    }
    
    private static boolean isUnsignedShort(int ushort) {
        return ushort == Short.toUnsignedInt((short)ushort);
    }
    
    private static final int MAJOR_BASE = 44;
    private static final int MAJOR_PREVIEW = 56;
    private static final int PREVIEW = 0xffff;
        
    public int toASM() {
        return minor << 16 | major;
    }

    public boolean isPreview() {
        return minor == PREVIEW && compareTo(V12) >= 0;
    }

    public String asJvm() {
        return String.format("%d.%d",major, minor);
    }
    
    public String asJava() {
        return name();
    }

    public String asClassFile() {
        return name().replace("V1_", "V");
    }
    
    private void checkValhalla() {
        if (OPTION(VALHALLA) && !supports(Feature.valhalla)) {
           LOG(M602, this); // "Version %s certainly does not support valhalla" 
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)",asJava(),asJvm());
    }
    
    private static final Map<String,JvmVersion> PARSE_MAP;
    
    public final static JvmVersion MIN_VERSION = V1_0_2;
    public final static JvmVersion DEFAULT_VERSION = V21;
    public final static JvmVersion SUPPORTED_VERSION = V24;
    public final static JvmVersion MAX_VERSION;

    static {
        PARSE_MAP = new HashMap<>();
        JvmVersion last = null;
        for (JvmVersion version:values()) {
            assert last == null
                    || version == V1_6  && last == V1_6JSR
                    || last.release < version.release
                    // "incorrect order: last = %s this = %s"
                    :M94.format(last,version);
            PARSE_MAP.put(version.asJava(), version);
            PARSE_MAP.put(version.asClassFile(), version);
            last = version;
        }
        assert PREVIEW == ClassFile.PREVIEW_MINOR_VERSION;
        int max = ClassFile.latestMajorVersion();
        String maxstr = String.format("V%d_PREVIEW", max);
        JvmVersion thisversion = PARSE_MAP.get(maxstr);
        if (thisversion == null) {
            thisversion = SUPPORTED_VERSION;
        }
        MAX_VERSION = thisversion;
    }

    public static JvmVersion getVersionInstance(String verstr) {
        JvmVersion version = PARSE_MAP.get(verstr.toUpperCase());
         if (version == null) {
            version = DEFAULT_VERSION;
            LOG(M147,verstr, version);   // "unknown Java version %s - %s used"
        }
        if (version.compareTo(MIN_VERSION) < 0) {
            LOG(M171,version,MIN_VERSION,MAX_VERSION,MIN_VERSION);  // "version %s outside range [%s,%s] - %s used"
            version = MIN_VERSION;
        } else if (version.compareTo(MAX_VERSION) > 0) {
            LOG(M171,version,MIN_VERSION,MAX_VERSION, MAX_VERSION);  // "version %s outside range [%s,%s] - %s used"
            version = MAX_VERSION;
        }
        version.checkSupported();
        version.checkValhalla();
        return version;
    }
    
    public static JvmVersion fromASM(int release) {
        int major = release & 0xffff;
        int minor = release >>>16;
        return from(major, minor);
    }
    
    public static JvmVersion from(int major, int minor) {
        if (!isUnsignedShort(major)) {
            // "invalid major version - %s"
            LOG(M20,major);
        }
        if (!isUnsignedShort(minor)) {
            // "invalid minor version - %s"
            LOG(M21,minor);
        }
        
        return from((major << 16) | minor);
    }
    
    private static JvmVersion from(int majmin) {
        long release = Integer.toUnsignedLong(majmin);
        JvmVersion last = values()[0];
        for (JvmVersion version:values()) {
            if (release == last.release) {
                break;
            }
            if (release < version.release) {
                // "unknown release (major = %d, minor = %d): used %s"
                LOG(M200, release >>> 16, release & 0xffff, last);
                break;
            }
            last = version;
        }
        last.checkValhalla();
        return last;
    }
    
    public void checkSupported() {
        if (isPreview() || compareTo(SUPPORTED_VERSION) > 0) {
            LOG(M72,this); // "version %s may not be fully supported"
        }
    }
    
    public boolean supports(JvmVersioned versioned) {
        return versioned.range().isSupportedBy(this);
    }
    
    public boolean checkSupports(JvmVersioned versioned) {
        boolean supported = supports(versioned);
        if (supported) {
            if (versioned.range().isDeprecated(this)) {
                //"%s is deprecated in version %s"
                LOG(M263,versioned,this);
            }
        } else if (versioned == Directive.dir_stack) {
            // "%s has been used for checking but has not been written to class file as only supported %s"
            LOG(M149, versioned, versioned.range());
        } else {
            // "Version %s does not support %s (supported %s)"
            LOG(M57,this,versioned,versioned.range());
        }
        return supported;
    }

}

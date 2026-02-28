package com.github.david32768.jynxfree.jvm;

import java.util.EnumSet;
import java.util.Objects;

import static com.github.david32768.jynxfree.jvm.JvmVersion.MIN_VERSION;
import static com.github.david32768.jynxfree.jvm.JvmVersion.NEVER;

public class JvmVersionRange {

    public static final JvmVersionRange UNLIMITED = 
            new JvmVersionRange(EnumSet.noneOf(JvmVersion.class), MIN_VERSION, NEVER, NEVER);

    private final EnumSet<JvmVersion> preview;
    private final JvmVersion start;
    private final JvmVersion deprecate;
    private final JvmVersion end;
    
    public JvmVersionRange (EnumSet<JvmVersion> preview, JvmVersion start, JvmVersion deprecate, JvmVersion end) {
        assert Objects.nonNull(preview);
        assert Objects.nonNull(start);
        assert Objects.nonNull(deprecate);
        assert Objects.nonNull(end);

        assert !start.isPreview(): "start = " + start;
        assert preview.stream().allMatch(v -> v.isPreview() && v.compareTo(start) < 0):"not all preview " + preview;

        assert start.compareTo(end) <= 0;
        assert deprecate.compareTo(start) >= 0 && deprecate.compareTo(end) <= 0;

        this.preview = preview;
        this.start = start;
        this.deprecate = deprecate;
        this.end = end;
    }
    
    public boolean isSupportedBy(JvmVersion jvmversion) {
        return jvmversion.compareTo(start) >= 0 && jvmversion.compareTo(end) < 0
                || preview.contains(jvmversion);
    }

    public boolean isDeprecated(JvmVersion jvmversion) {
        assert isSupportedBy(jvmversion);
        return deprecate.compareTo(end) < 0 && jvmversion.compareTo(deprecate) >= 0;
    }
    
    @Override
    public String toString() {
        String limitsmsg = "";
        if (start == NEVER) {
            return "preview from " + preview.toString();
        }
        if (start != JvmVersion.MIN_VERSION) limitsmsg += "from " + start.toString();
        if (start != JvmVersion.MIN_VERSION && end != JvmVersion.NEVER) limitsmsg += " to ";
        if (end != JvmVersion.NEVER) limitsmsg += " before " + end.toString();
        if (limitsmsg.isEmpty()) {
            return "UNLIMITED";
        }
        return limitsmsg;
    }
}

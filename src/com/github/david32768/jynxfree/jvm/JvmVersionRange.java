package com.github.david32768.jynxfree.jvm;

import java.util.EnumSet;
import java.util.Objects;

import static com.github.david32768.jynxfree.jvm.JvmVersion.MIN_VERSION;
import static com.github.david32768.jynxfree.jvm.JvmVersion.NEVER;
import static com.github.david32768.jynxfree.my.Message.M359;
import static com.github.david32768.jynxfree.my.Message.M360;
import static com.github.david32768.jynxfree.my.Message.M361;
import static com.github.david32768.jynxfree.my.Message.M363;
import static com.github.david32768.jynxfree.my.Message.M364;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public class JvmVersionRange {

    public static final JvmVersionRange UNLIMITED = 
            new JvmVersionRange(EnumSet.noneOf(JvmVersion.class), MIN_VERSION, NEVER, NEVER);

    private final EnumSet<JvmVersion> preview;
    private final JvmVersion start;
    private final JvmVersion deprecate;
    private final JvmVersion end;
    
    public JvmVersionRange (EnumSet<JvmVersion> preview, JvmVersion start, JvmVersion deprecate, JvmVersion end) {
        
        if (preview == null || !preview.stream().allMatch(v -> v.isPreview())) {
            // "preview = %s must be preview version(s)"
            throw new LogIllegalArgumentException(M360, preview);
        }        
        if (start == null || start.isPreview()) {
            // "start = %s cannot be a preview version or null"
            throw new LogIllegalArgumentException(M359, start);
        }
        Objects.requireNonNull(deprecate);
        Objects.requireNonNull(end);
        
        if (!preview.stream().allMatch(v -> v.compareTo(start) < 0)) {
            // "all preview versions %s must be less than start = %s"
            throw new LogIllegalArgumentException(M361, preview, start);
        }
        if (start.compareTo(end) > 0) {
            // "start = %s must be less than end = %s"
            throw new LogIllegalArgumentException(M363, start, end);
        }
        if (deprecate.compareTo(start) < 0 || deprecate.compareTo(end) > 0) {
            // "deprecate = %s must be in range [start,end]: start = %s end = %s"
            throw new LogIllegalArgumentException(M364, deprecate, start, end);
        }

        super();

        this.preview = preview;
        this.start = start;
        this.deprecate = deprecate;
        this.end = end;
    }
    
    public enum State {
        BEFORE,
        PREVIEW,
        ACTIVE,
        DEPRECATED,
        AFTER,
        ;
    }
    
    public State stateFor(JvmVersion version) {
        if (preview.contains(version)) {
            return State.PREVIEW;
        }
        if (version.compareTo(start) < 0) {
            return State.BEFORE;
        }
        if (version.compareTo(end) >= 0) {
            return State.AFTER;
        }
        if (version.compareTo(deprecate) >= 0) {
            return State.DEPRECATED;
        }
        return State.ACTIVE;
    }
    
    public boolean isSupportedBy(JvmVersion jvmversion) {
        State state = stateFor(jvmversion);
        return switch (state) {
            case PREVIEW, ACTIVE, DEPRECATED -> true; 
            case BEFORE, AFTER -> false;
        };
    }

    public boolean isDeprecated(JvmVersion jvmversion) {
        State state = stateFor(jvmversion);
        return state == State.DEPRECATED;
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

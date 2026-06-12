package com.github.david32768.jynxfree.jvm;

public interface JvmVersioned {

    public JvmVersionRange range();

    public default boolean isSupportedBy(JvmVersion jvmversion) {
        return range().isSupportedBy(jvmversion);
    }

    public default boolean isDeprecatedIn(JvmVersion jvmversion) {
        return range().isDeprecated(jvmversion);
    }

}

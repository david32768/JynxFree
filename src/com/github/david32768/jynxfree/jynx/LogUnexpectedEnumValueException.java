package com.github.david32768.jynxfree.jynx;

import static com.github.david32768.jynxfree.jynx.Global.LOG;
import static com.github.david32768.jynxfree.my.Message.M342;

public class LogUnexpectedEnumValueException extends AssertionError {

    private static final long serialVersionUID = 1L;
    
    public LogUnexpectedEnumValueException(Enum<?> unexpectedEnum) {
        // "enum %s in class %s unexpected"
        LOG(M342, unexpectedEnum.name(), unexpectedEnum.getClass());
    }

}

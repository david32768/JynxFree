package com.github.david32768.jynxfree.jynx;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

public class LogAssertionError extends AssertionError {

    private static final long serialVersionUID = 1L;
    
    public LogAssertionError(Message msg,Object... args) {
        LOG(msg,args);
    }

}

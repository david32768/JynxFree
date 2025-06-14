package com.github.david32768.jynxfree.jynx;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

public class LogIllegalStateException extends IllegalStateException {

    private static final long serialVersionUID = 1L;
    
    public LogIllegalStateException(Message msg,Object... args) {
        LOG(msg,args);
    }

}

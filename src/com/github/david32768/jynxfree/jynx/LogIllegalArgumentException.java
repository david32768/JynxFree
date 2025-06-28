package com.github.david32768.jynxfree.jynx;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

public class LogIllegalArgumentException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;
    
    public LogIllegalArgumentException(JynxMessage msg,Object... args) {
        LOG(msg,args);
    }

}

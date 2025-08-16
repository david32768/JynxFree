package com.github.david32768.jynxfree.jynx;

import static com.github.david32768.jynxfree.jynx.Global.LOG;

public class LogUnsupportedOperationException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;
    
    public LogUnsupportedOperationException(JynxMessage msg,Object... args) {
        LOG(msg,args);
    }

}

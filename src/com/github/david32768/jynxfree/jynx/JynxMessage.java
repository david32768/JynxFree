package com.github.david32768.jynxfree.jynx;

import com.github.david32768.jynxfree.jynx.LogMsgType;

public interface JynxMessage {

    String format(Object... objs);

    String getFormat();

    LogMsgType getLogtype();
    
    String name();
    
}

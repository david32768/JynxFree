package com.github.david32768.jynxfree.jynx;

public enum LogMsgType {

    // errors in ascending severity
    FINEST('t'),
    FINER('r'),
    FINE('e'),
    BLANK(' '),
    ENDINFO('C'),
    INFO('I'),
    LINE('L'),
    STYLE('T'),
    WARNING('W'),
    ERROR('E'),
    SEVERE('S'),
    ;
    
    private final char abbrev;

    private LogMsgType(char abbrev) {
        this.abbrev = abbrev;
    }

    public String prefix(String msgname) {
        if (abbrev == ' ') {
            return "";
        }
        return abbrev + msgname.substring(1) + ": ";
    }

    public LogMsgType up() {
        return switch(this) {
            case FINEST, FINER, FINE, ENDINFO -> LINE;
            case WARNING -> ERROR;
            case ERROR -> SEVERE;
            case INFO, LINE, BLANK, STYLE, SEVERE -> this;
        };
    }

    public LogMsgType supress() {
        return switch(this) {
            case FINEST, FINER, FINE, ENDINFO, WARNING, INFO, LINE, STYLE -> FINEST;
            case BLANK, ERROR, SEVERE -> this;
        };
    }

}

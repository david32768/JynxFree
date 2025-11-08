package com.github.david32768.jynxfree.jynx;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static com.github.david32768.jynxfree.jynx.Global.OPTION;

import static com.github.david32768.jynxfree.my.Message.M104;
import static com.github.david32768.jynxfree.my.Message.M131;
import static com.github.david32768.jynxfree.my.Message.M84;
import static com.github.david32768.jynxfree.my.Message.M85;

public class Logger {

    private static final int MAX_ERRORS = 20;

    private final Deque<String> contexts;
    private final Deque<String> lines;
    private final Set<String> endinfo;
    private final String type;

    private String currentLine;
    private String lastErrorLine;
    
    private int errct;
    private final int maxerr;

    Logger(String type) {
        this.contexts = new ArrayDeque<>();
        this.lines = new ArrayDeque<>();
        this.endinfo = new LinkedHashSet<>(); // so order of info messages is reproducible
        this.type = type;
        this.errct = 0;
        this.maxerr = MAX_ERRORS;
    }

    public int numErrors() {
        return errct;
    }

    public void setLine(String line) {
        this.currentLine = line;
    }
    
    public void pushContext() {
        contexts.push(currentLine); // addFirst
    }
    
    public void popContext() {
        String line = contexts.pop(); // removeFirst
    }

    public void pushCurrent() {
        lines.push(currentLine); // addFirst
    }
    
    public void popCurrent() {
        currentLine = lines.pop(); // removeFirst
    }

    private void printInfo(JynxMessage msg, Object... args) {
        System.err.println(msg.format(args));
    }
    
    private void printLineMessage(JynxMessage msg, Object... args) {
        if (Objects.equals(currentLine,lastErrorLine)) {
        } else {
            System.err.println();
            String context = contexts.peekFirst();
            if (context != null && !Objects.equals(context,currentLine)) {
                System.err.println(context);
            }
            if (currentLine != null) {
                System.err.println(currentLine);
            }
        }
        lastErrorLine = currentLine;
        printInfo(msg, args);
    }

    private void printError(JynxMessage msg, Object... args) {
        printLineMessage(msg, args);
        errct++;
    }

    private void addEndInfo(JynxMessage msg, Object... args) {
        endinfo.add(msg.format(args));
    }
    
    boolean printEndInfo(Object parms){
        printEndInfo();
        boolean ok = errct == 0;
        if (ok) {
             // "%s of %s completed successfully"
            printInfo(M104, type, parms);
        } else {
             // "%s of %s completed  unsuccesfully - number of errors is %d"
            printInfo(M131, type, parms, errct);
        }
        currentLine = null;
        errct = 0;
        return ok;
    }

    private void printEndInfo(){
        System.err.println();
        for (String msg:endinfo) {
            System.err.println(msg);
        }
        endinfo.clear();
    }

    private static LogMsgType msgType(JynxMessage msg) {
        LogMsgType logtype = msg.getLogtype();
        if (OPTION(GlobalOption.INCREASE_MESSAGE_SEVERITY)) {
            logtype = logtype.up();
        }
        if (OPTION(GlobalOption.SUPPRESS_WARNINGS)) {
            logtype = logtype.supress();
        }
        return logtype;
    }
    
    @SuppressWarnings("fallthrough")
    void log(JynxMessage msg, Object... objs) {
       LogMsgType logtype = msgType(msg);
        switch (logtype) {
            case SEVERE -> {
                printError(msg,objs);
                printInfo(M84,type); // "%s terminated because of severe error"
                throw new SevereError();
            }
            case LINE, STYLE, WARNING -> printLineMessage(msg,objs);
            case ERROR -> {
                printError(msg,objs);
                if (errct > maxerr) {
                    printInfo(M85,type); // "%s terminated because of too many errors"
                    throw new SevereError();
                }
            }
            case ENDINFO -> addEndInfo(msg, objs);
            case INFO -> printInfo(msg,objs);
            case BLANK -> printInfo(msg,objs);
            case FINE, FINER, FINEST -> {
                // use INCREASE_MESSAGE_SEVERITY to print
            }
            default -> throw new LogUnexpectedEnumValueException(logtype);
        }
                
    }

    void log(String line, JynxMessage msg, Object... objs) {
        pushCurrent();
        setLine(line);
        log(msg,objs);
        popCurrent();
    }

}

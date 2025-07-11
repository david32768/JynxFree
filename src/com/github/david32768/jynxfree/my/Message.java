package com.github.david32768.jynxfree.my;

import static com.github.david32768.jynxfree.jynx.LogMsgType.*;

import com.github.david32768.jynxfree.jynx.JynxMessage;
import com.github.david32768.jynxfree.jynx.LogMsgType;

public enum Message implements JynxMessage {

    M0(BLANK,"Jynx version %s; maximum Java version is %s"),
    M1(BLANK,"display help message"),
    M2(BLANK,"display version information"),
    M3(BLANK,"program terminated because of errors"),
    M4(BLANK,"%nJynx %s %s; Java runtime version %s"),
    M5(BLANK,"  %s%n"),
    M6(BLANK,"Options are:%n"),
    M7(BLANK,"use SYSIN as input file"),
    M8(BLANK,"check that called methods or used fields exist (on class path)"),
    M9(BLANK,"generate line numbers"),
    M10(BLANK,"warn if label unreferenced or alias"),
    M11(BLANK,"let simple verifier use Class.forName() for non-java classes"),
    M12(BLANK,"%nUsage:%n"),
    M13(BLANK,"print stack trace(s)"),
    M14(BLANK,"if necessary reduces JVM release to maximum supported by ASM version"),
    M15(BLANK,"warn if names non-standard"),
    M16(BLANK,"use ASM BasicVerifier instead of ASM SimpleVerifier"),
    M17(BLANK,"prints constant pool, instructions and other detail"),
    M18(BLANK,"do not produce annotations"),
    M19(BLANK,"use supplied stack map instead of ASM generated"),
    M20(ERROR,"invalid major version - %s"),
    M21(ERROR,"invalid minor version - %s"),
    M23(BLANK,"print (ASMifier) trace"),
    M24(BLANK,"%s %s     gives list of options"),
    M25(BLANK,"treat warnings as errors etc."),
    M26(BLANK,"invalid main-option name - %s"),
    M27(BLANK,"suppress warnings"),
    M28(ERROR,"no args have been specified for main option %s"),
    M29(BLANK,"do not produce debug info"),
    M30(BLANK,"do not produce stack map"),
    M32(ERROR,"%s is not a valid option"),
    M39(BLANK,"do not produce code"),
    M44(BLANK,"local variables are symbolic not absolute integers"),
    M51(BLANK,"do not output class file"),
    M57(ERROR,"Version %s does not support %s (supported %s)"),
    M61(ERROR,"invalid stack frame type(%s) - %s assumed"),
    M62(ERROR,"macro nest level is negative or exceeds %d"),
    M63(ERROR,"Loading of %s not supported in %s"),
    M64(STYLE,"final static field name (%s) is not in uppercase"),
    M66(ERROR,"invalid %s: %s"),
    M68(WARNING,"Quoted string not followed by white space; blank inserted before '%c'"),
    M69(ERROR,"Embedded naked quote"),
    M70(ERROR,"cannot range check floating point numbers"),
    M72(WARNING,"version %s may not be fully supported"),
    M73(WARNING,"irrelevant option %s ignored"),
    M74(BLANK,"use java.lang.classfile"),
    M76(ERROR,"unknown handle tag: %d"),
    M77(ERROR,"%s value %d is not in range [%d,%d]"),
    M80(ERROR,"Bad octal sequence"),
    M83(ERROR,"Bad escape sequence"),
    M84(INFO,"%s terminated because of severe error"),
    M85(INFO,"%s terminated because of too many errors"),
    M91(ERROR,"invalid minor version for major version (spec table 4.1A) - %s"),
    M94(ERROR,"incorrect order: last = %s this = %s"),
    M101(ERROR,"unknown handle mnemonic: %s"),
    M104(BLANK,"class %s %s completed successfully"),
    M107(ERROR,"unknown access flag (%#04x) in context %s ignored"),
    M108(ERROR,"type index (%d) outside range [0 - %d]"),
    M110(ERROR,"access flag(s) %s in context %s not valid for version %s"),
    M114(ERROR,"Requires at most one of {%s} specified"),
    M118(ERROR,"Requires all of {%s} specified"),
    M120(ERROR,"Requires only one of {%s} specified"),
    M125(ERROR,"Requires none of {%s} specified"),
    M129(ERROR,"invalid typecode - %d"),
    M131(BLANK,"class %s %s completed  unsuccesfully - number of errors is %d"),
    M136(ERROR,"Extraneous directive %s"),
    M147(WARNING,"unknown Java version %s - %s used"),
    M148(BLANK,"optimise switch instruction size"),
    M149(ENDINFO,"%s has been used for checking but has not been written to class file as only supported %s"),
    M150(ERROR,"expected equal values for index length = %d numind = %d"),
    M160(ERROR,"invalid access flags %s for %s are dropped"),
    M161(ERROR,"%s: jynx value (%d) does not agree with classfile value(%d)"),
    M165(SEVERE,"Directive in wrong place; Current state = %s%n  Expected state was one of %s"),
    M170(ERROR,"invalid type annotation directive - %s"),
    M171(WARNING,"version %s outside range [%s,%s] - %s used"),
    M177(ERROR,"missing %s"),
    M178(ERROR,"invalid type ref sort - %d"),
    M186(ERROR,"%s for %s must be %s"),
    M195(WARNING,"inner class name (%s) does not contain '$'"),
    M197(ERROR,"inner class cannot be module"),
    M200(WARNING,"unknown release (major = %d, minor = %d): used %s"),
    M202(ERROR,"unused field(s) in typeref not zero"),
    M218(INFO,"SYSIN will be used as input"),
    M219(ERROR,"wrong number of parameters after options %s"),
    M226(ERROR,"invalid access flags %s for component"),
    M235(ERROR,"%s method appears in an interface"),
    M263(ENDINFO,"%s is deprecated in version %s"),
    M271(ERROR,"incomplete quoted string %s"),
    M283(ERROR,"%s for %s cannot be %s"),
    M286(SEVERE,"%s is not (a known) class"),
    M289(SEVERE,"file %s does not exist"),
    M295(ERROR,"%s is invalid: %s"),
    M298(WARNING,"%s of %s failed"),
    M327(INFO,"added: %s %s"),
    M331(ERROR,"MainOption service for %s not found"),
    M332(BLANK,"GlobalOption abbreviations are not unique after transform"),
    M333(BLANK,"GlobalOption names are not unique after transform"),
    M334(ERROR,"abbrev '%s' for option %s has invalid name"),
    M336(ERROR,"option '%s' has invalid name"),
    M525(ERROR,"CP entry is %s but should be one of %s"),
    M528(BLANK,"omit start and length values"),
    M601(BLANK,"Valhalla - limited support; may change"),
    M602(ERROR,"Version %s certainly does not support valhalla"),
    M609(BLANK,"do not print stack after each instruction"),
    M902(ERROR,"unknown ASM stack frame type (%d)"),
    M904(ERROR,"unknown JVM stack frame type (%d)"),
    M905(ERROR,"unexpected StringState %s"),
    M999(SEVERE,"%s"),
    ;

    private final LogMsgType logtype;
    private final String format;
    private final String msg;

    private Message(LogMsgType logtype, String format) {
        this.logtype = logtype;
        this.format = logtype.prefix(name()) + format;
        this.msg = format;
    }
    
    
    private Message(String format) {
        this(ERROR,format);
    }

    @Override
    public String format(Object... objs) {
        return String.format(format,objs);
    }

    @Override
    public String getFormat() {
        return msg;
    }
    
    @Override
    public LogMsgType getLogtype() {
        return logtype;
    }
    
}

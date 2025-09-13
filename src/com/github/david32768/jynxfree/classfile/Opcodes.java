package com.github.david32768.jynxfree.classfile;

import java.lang.classfile.Opcode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.github.david32768.jynxfree.my.Message.M643;

import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public class Opcodes {
    
    private Opcodes(){}
    
    public static final int WIDE = 196;
    
    private static final List<Opcode> OPCODES;
    private static final List<Opcode> WIDE_OPCODES; // prefixed by wide
    private static final int LIMIT = Opcode.JSR_W.bytecode();
    
    static {
        var opcodes = Opcode.values();
        // order of Opcode is not specified although in bytecode order
        Arrays.sort(opcodes, Comparator.comparing(Opcode::bytecode));
        var list = List.of(opcodes);
        OPCODES = list.subList(0, LIMIT);
        WIDE_OPCODES = list.subList(LIMIT, list.size());
    }
    
    public static Opcode of(int bytecode) {
        Objects.checkIndex(bytecode, LIMIT);
        int index = bytecode < WIDE? bytecode: bytecode - 1;
        Opcode result = OPCODES.get(index);
        assert result.bytecode() == bytecode;
        return result;
    }
    
    public static Opcode widePrepended(int bytecode) {
        Objects.checkIndex(bytecode, LIMIT);
        int wbc = WIDE << 8 + bytecode;
        return WIDE_OPCODES.stream()
                    .filter(opcode -> opcode.bytecode() == wbc)
                    .findFirst()
                    .orElseThrow();
    }
    
    public static boolean isImmediate(Opcode opcode) {
        return opcode.sizeIfFixed() == 1;
    }
    
    public static boolean isWide(Opcode opcode) {
        return suffix(opcode).equals("W");
//        return opcode.isWide(); // WOULD RETURN ONLY PSEUDO INSTRUCTIONS i.e. PREFIXED WITH WIDE
    }
    
    public static int numericSuffix(Opcode opcode) {
        String suffix = suffix(opcode);
        return switch(suffix) {
            case "m1" -> -1;
            case "0" -> 0;
            case "1" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            default -> {
                String msg = String.format("non-numeric suffix %s", suffix);
                throw new IllegalArgumentException(msg);
            }
        };
    }
    
    private static String suffix(Opcode opcode) {
        String name = opcode.name();
        int index = name.lastIndexOf('_');
        if (index < 0) {
            return "";
        }
        return name.substring(index + 1);
    }
    
    private static final EnumSet<Opcode> RETURNS = EnumSet.of(
            Opcode.ARETURN,
            Opcode.IRETURN,
            Opcode.LRETURN,
            Opcode.FRETURN,
            Opcode.DRETURN,
            Opcode.RETURN
    );
    
    
    public static boolean isReturn(Opcode opcode) {
        return RETURNS.contains(opcode);
    }
    
    private static final EnumSet<Opcode> GO = EnumSet.of(
            Opcode.ATHROW,
            Opcode.GOTO,
            Opcode.GOTO_W,
            Opcode.LOOKUPSWITCH,
            Opcode.RET,
            Opcode.RET_W,
            Opcode.TABLESWITCH
    );
    
    public static boolean isUnconditional(Opcode opcode) {
        return GO.contains(opcode) || isReturn(opcode);
    }
    
    public static Opcode oppositeBranch(Opcode opcode) {
        return switch (opcode) {
            case IFNULL -> Opcode.IFNONNULL;
            case IFNONNULL -> Opcode.IFNULL;
            case IFEQ -> Opcode.IFNE;
            case IFNE -> Opcode.IFEQ;
            case IFLT -> Opcode.IFGE;
            case IFGE -> Opcode.IFLT;
            case IFLE -> Opcode.IFGT;
            case IFGT -> Opcode.IFLE;
            case IF_ACMPEQ -> Opcode.IF_ACMPNE;
            case IF_ACMPNE -> Opcode.IF_ACMPEQ;
            case IF_ICMPEQ -> Opcode.IF_ICMPNE;
            case IF_ICMPNE -> Opcode.IF_ICMPEQ;
            case IF_ICMPLT -> Opcode.IF_ICMPGE;
            case IF_ICMPGE -> Opcode.IF_ICMPLT;
            case IF_ICMPLE -> Opcode.IF_ICMPGT;
            case IF_ICMPGT -> Opcode.IF_ICMPLE;
            default -> 
                // "no opposite branch for %s"
                throw new LogIllegalArgumentException(M643, opcode);
        };
    }
}

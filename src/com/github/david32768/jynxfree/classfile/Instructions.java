package com.github.david32768.jynxfree.classfile;

import static java.lang.classfile.Opcode.*;

import java.lang.classfile.Instruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.Opcode;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;

public class Instructions {
    
    public enum OperationType {
       ARRAY_LENGTH(ARRAYLENGTH),
       UNARY(INEG, LNEG, FNEG, DNEG),
       SHIFT(ISHL, ISHR, IUSHR, LSHL, LSHR, LUSHR),
       COMPARE(LCMP, FCMPG, FCMPL, DCMPG, DCMPL),
       BINARY(IADD, LADD, FADD, DADD,
                ISUB, LSUB, FSUB, DSUB,
                IMUL, LMUL, FMUL, DMUL,
                IDIV, LDIV, FDIV, DDIV,
                IREM, LREM, FREM, DREM,
                IAND, LAND, IOR, LOR, IXOR, LXOR),
       ;
       
       private final EnumSet<Opcode> opcodes;

        private OperationType(Opcode opcode1, Opcode... opcodes) {
            this.opcodes = EnumSet.of(opcode1, opcodes);
        }
        
        public static OperationType of(OperatorInstruction opinst) {
            Opcode op = opinst.opcode();
            return Stream.of(values())
                    .filter(ot -> ot.opcodes.contains(op))
                    .findAny()
                    .orElseThrow();
        }
    }

    public enum BranchType {
        COMMAND(GOTO, GOTO_W),
        AUNARY(IFNULL, IFNONNULL),
        IUNARY(IFEQ, IFNE, IFLT, IFGT, IFLE, IFGE),
        ABINARY(IF_ACMPEQ, IF_ACMPNE),
        IBINARY(IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGT, IF_ICMPGE, IF_ICMPLE),
        ;

        private final EnumSet<Opcode> opcodes;

        private BranchType(Opcode opcode1, Opcode... opcodes) {
            this.opcodes = EnumSet.of(opcode1, opcodes);
        }
        
        public static BranchType of(BranchInstruction opinst) {
            Opcode op = opinst.opcode();
            return Stream.of(values())
                    .filter(ot -> ot.opcodes.contains(op))
                    .findAny()
                    .orElseThrow();
            
        }

    }

    private Instructions() {}
    
    private static final EnumSet<Opcode> UNCONDITIONAL = EnumSet.of(GOTO, GOTO_W,
            TABLESWITCH, LOOKUPSWITCH, ATHROW,
            ARETURN, IRETURN , LRETURN, FRETURN, DRETURN, RETURN,
            RET, RET_W);
    
    public static boolean isUnconditional(Opcode op) {
        return UNCONDITIONAL.contains(op);
    }

    public static int sizeOfAt(Instruction instruction, int offset) {
        var op = instruction.opcode();
        int padding = 3 - (offset & 3);
        return switch(instruction) {
            case LookupSwitchInstruction swinst -> 1 + padding + 4 + 4 + 8*swinst.cases().size();                    
            case TableSwitchInstruction swinst -> 1 + padding + 4 + 4 + 4 + 4*swinst.cases().size();
            default -> op.sizeIfFixed();
        };
    }

    private static final int GOTO_W_SIZE = Opcode.GOTO_W.sizeIfFixed();
    private static final int JSR_WIDEN_COST = Opcode.JSR_W.sizeIfFixed() - Opcode.JSR.sizeIfFixed();
    private static final int GOTO_WIDEN_COST = Opcode.GOTO_W.sizeIfFixed() - Opcode.GOTO.sizeIfFixed();
    
    public static int largeBranchAdjustment(Instruction instruction, int offset) {
        var op = instruction.opcode();
        int padding = 3 - (offset & 3);
        int extrapadding = 3 - padding; // in case long branches converted to short
        return switch(op) {
            case TABLESWITCH -> extrapadding;
            case LOOKUPSWITCH -> extrapadding;
            case JSR -> JSR_WIDEN_COST;
            case JSR_W -> 0;
            case GOTO -> GOTO_WIDEN_COST;
            case GOTO_W -> 0;
            default -> instruction instanceof BranchInstruction? GOTO_W_SIZE: 0;
        };
    }

    public static Optional<Integer> slot(Instruction inst) {
        Integer slot = switch(inst) {
            case StoreInstruction store -> store.slot();
            case LoadInstruction load -> load.slot();
            case IncrementInstruction incr -> incr.slot();
            case DiscontinuedInstruction.RetInstruction ret -> ret.slot();
            default -> null;
        };
        return Optional.ofNullable(slot);
    }

    public static Opcode oppositeBranch(Opcode opcode) {
        return switch (opcode) {
            case IFNULL -> IFNONNULL;
            case IFNONNULL -> IFNULL;
            case IFEQ -> IFNE;
            case IFNE -> IFEQ;
            case IFLT -> IFGE;
            case IFGE -> IFLT;
            case IFLE -> IFGT;
            case IFGT -> IFLE;
            case IF_ACMPEQ -> IF_ACMPNE;
            case IF_ACMPNE -> IF_ACMPEQ;
            case IF_ICMPEQ -> IF_ICMPNE;
            case IF_ICMPNE -> IF_ICMPEQ;
            case IF_ICMPLT -> IF_ICMPGE;
            case IF_ICMPGE -> IF_ICMPLT;
            case IF_ICMPLE -> IF_ICMPGT;
            case IF_ICMPGT -> IF_ICMPLE;
            default -> throw new IllegalArgumentException("no opposite branch for " + opcode);
        };
    }
}

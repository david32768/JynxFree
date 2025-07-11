package com.github.david32768.jynxfree.classfile;

import static java.lang.classfile.Opcode.*;

import java.lang.classfile.instruction.*;

import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


public class TypeKindStack implements InstructionVisitor {

    private record TwoSlot(TypeKind top, TypeKind next) {
    
        public TwoSlot {
            Objects.requireNonNull(top);
            Objects.requireNonNull(next);
            if (top.slotSize() + next.slotSize() != 2) {
                String msg = String.format(
                        "sum of slot sizes for top (%s = %d) and next (%s = %d) not equal to 2",
                        top, top.slotSize(), next, next.slotSize());
                throw new IllegalArgumentException(msg);
            }
            if (top == TypeKind.VOID) {
                String msg = "top TypeKind cannot be VoidType";
                throw new IllegalArgumentException(msg);
            }
        }
    }
    
    private record OneSlot(TypeKind top) {
    
        public OneSlot {
            Objects.requireNonNull(top);
            if (top.slotSize() != 1) {
                String msg = String.format("top slot size for (%s = %d) not 1",
                        top, top.slotSize());
                throw new IllegalArgumentException(msg);
            }
       }
    }
    
    private final ArrayList<TypeKind> astack;
    
    private int maxstack;
    private int current;

    public TypeKindStack() {
        this.astack = new ArrayList<>();
        this.maxstack = 0;
        this.current = 0;
    }

    public int maxStack() {
        return maxstack;
    }

    public void clear() {
        astack.clear();
        current = 0;
    }
    
    public boolean isEmpty() {
        return astack.isEmpty();
    }
    
    public List<TypeKind> toList() {
        return List.copyOf(astack);
    }

    public Stream<TypeKind> stream() {
        return astack.stream();
    }
    
    private void pushKind(TypeKind typeKind) {
        if (typeKind == TypeKind.VOID) {
            return;
        }
        astack.add(typeKind.asLoadable());
        current += typeKind.slotSize();
        maxstack = Math.max(maxstack, current);
    }
    
    public void set(List<TypeKind> list) {
        if (astack.isEmpty()) {
            for (var kind : list) {
                pushKind(kind);
            }
        } else {
            String msg = "stack must be empty to use set()";
            throw new IllegalStateException(msg);
        }
    }

    private void pushInt() {
        pushKind(TypeKind.INT);
    }
    
    private void pushReference() {
        pushKind(TypeKind.REFERENCE);
    }

    private void push1(OneSlot oneslot) {
        pushKind(oneslot.top());
    }
    
    private void push2(TwoSlot twoslot) {
        pushKind(twoslot.next());
        pushKind(twoslot.top());
    }
    
    public TypeKind peek() {
        int last = astack.size() - 1;
        return astack.get(last);
    }
    
    private TypeKind pop() {
        int last = astack.size() - 1;
        var result = astack.remove(last);
        current -= result.slotSize();
        return result;
    }
    
    private void popKind(TypeKind typeKind) {
        if (typeKind == TypeKind.VOID) {
            return;
        }
        TypeKind onStack = pop();
        if (onStack != typeKind.asLoadable()) {
            String msg = String.format("top of stack is %s but expected %s",
                    onStack, typeKind.asLoadable());
            throw new IllegalArgumentException(msg);
        }
    }
    
    private void popInt() {
        popKind(TypeKind.INT);
    }
    
    private void popReference() {
        popKind(TypeKind.REFERENCE);
    }
    
    private OneSlot pop1() {
        var top = pop();
        return new OneSlot(top);
    }
    
    private TwoSlot pop2() {
        var top = pop();
        var next = top.slotSize() == 2? TypeKind.VOID: pop();
        return new TwoSlot(top, next);
    }

    public void adjustForInstruction(Instruction instruction) {
        InstructionVisitor.visit(this, instruction);
    }

    @Override
    public void arrayLoad(Opcode op, ArrayLoadInstruction inst) {
        popInt();
        popReference();
        pushKind(inst.typeKind());
    }

    @Override
    public void arrayStore(Opcode op, ArrayStoreInstruction inst) {
        popKind(inst.typeKind());
        popInt();
        popReference();
    }

    @Override
    public void branch(Opcode op, BranchInstruction inst) {
        var type = Instructions.BranchType.of(inst);
        switch(type) {
            case COMMAND -> {}
            case AUNARY -> {
                popKind(TypeKind.REFERENCE);
            }
            case IUNARY -> {
                popKind(TypeKind.INT);
            }
            case ABINARY -> {
                popKind(TypeKind.REFERENCE);
                popKind(TypeKind.REFERENCE);
            }
            case IBINARY -> {
                popKind(TypeKind.INT);
                popKind(TypeKind.INT);
            }
        }
    }

    @Override
    public void constant(Opcode op, ConstantInstruction inst) {
        pushKind(inst.typeKind());
    }

    @Override
    public void convert(Opcode op, ConvertInstruction inst) {
        popKind(inst.fromType());
        pushKind(inst.toType());
    }

    @Override
    public void discontinued(Opcode op, DiscontinuedInstruction inst) {
        switch(inst) {
            case DiscontinuedInstruction.JsrInstruction _ -> {
                pushReference();
                // NB the stack for label is before the following pop 
                popReference();
            }
            case DiscontinuedInstruction.RetInstruction _ -> {}
        }
    }

    @Override
    public void field(Opcode op, FieldInstruction inst) {
        var kind = TypeKind.from(inst.typeSymbol());
        switch(op) {
            case GETFIELD -> {
                popReference();
                pushKind(kind);
            }
            case GETSTATIC -> {
                pushKind(kind);
            }
            case PUTFIELD -> {
                popKind(kind);
                popReference();
            }
            case PUTSTATIC -> {
                popKind(kind);
            }
            default -> {
                assert false:MISSING + op;
            }
        }
    }

    @Override
    public void invokeDynamic(Opcode op, InvokeDynamicInstruction inst) {
        var type = inst.typeSymbol();
        for (var desc : type.parameterList().reversed()) {
            var kind = TypeKind.from(desc);
            popKind(kind);
        }
        pushKind(TypeKind.from(type.returnType()));
    }

    @Override
    public void invoke(Opcode op, InvokeInstruction inst) {
        var type = inst.typeSymbol();
        for (var desc : type.parameterList().reversed()) {
            var kind = TypeKind.from(desc);
            popKind(kind);
        }
        switch (op) {
            case INVOKEINTERFACE, INVOKESPECIAL, INVOKEVIRTUAL -> {
                popReference();
            }
            default -> {
                assert op == Opcode.INVOKESTATIC:MISSING + op;
            }
        }
        pushKind(TypeKind.from(type.returnType()));
    }

    @Override
    public void increment(Opcode op, IncrementInstruction inst) {
    }

    @Override
    public void load(Opcode op, LoadInstruction inst) {
        pushKind(inst.typeKind());
    }

    @Override
    public void store(Opcode op, StoreInstruction inst) {
        popKind(inst.typeKind());
    }

    @Override
    public void lookupSwitch(Opcode op, LookupSwitchInstruction inst) {
        popInt();
    }

    @Override
    public void monitor(Opcode op, MonitorInstruction inst) {
        popReference();
    }

    @Override
    public void newMultiArray(Opcode op, NewMultiArrayInstruction inst) {
        for (int i = 0; i < inst.dimensions(); ++i) {
            popInt();
        }
        pushReference();
    }

    @Override
    public void newObject(Opcode op, NewObjectInstruction inst) {
        pushReference();
    }

    @Override
    public void newPrimitiveArray(Opcode op, NewPrimitiveArrayInstruction inst) {
        popInt();
        pushReference();
    }

    @Override
    public void newReferenceArray(Opcode op, NewReferenceArrayInstruction inst) {
        popInt();
        pushReference();
    }

    @Override
    public void nop(Opcode op, NopInstruction inst) {
    }

    @Override
    public void operator(Opcode op, OperatorInstruction inst) {
        var kind = inst.typeKind();
        var result = kind;
        var type = Instructions.OperationType.of(inst);
        switch (type) {
            case ARRAY_LENGTH -> {
                popReference();
            }
            case SHIFT -> {
                popInt();
                popKind(kind);
            }
            case UNARY -> {
                popKind(kind);
            }
            case COMPARE -> {
                popKind(kind);
                popKind(kind);
                result = TypeKind.INT;
            }
            case BINARY  -> {
                popKind(kind);
                popKind(kind);
            }
        }
        pushKind(result);
    }

    @Override
    public void return_(Opcode op, ReturnInstruction inst) {
        popKind(inst.typeKind());
    }

    @Override
    public void stack(Opcode op, StackInstruction inst) {
        switch(op) {
            case POP -> {
                pop1();
            }    
            case POP2 -> {
                pop2();
            }
            case DUP -> {
                var top = pop1();
                push1(top);
                push1(top);
            }
            case DUP_X1 -> {
                var top = pop1();
                var next = pop1();
                push1(top);
                push1(next);
                push1(top);
            }
            case DUP_X2 -> {
                var top = pop1();
                var next2 = pop2();
                push1(top);
                push2(next2);
                push1(top);
            }
            case DUP2 -> {
                var top2 = pop2();
                push2(top2);
                push2(top2);
            }
            case DUP2_X1 -> {
                var top2 = pop2();
                var next = pop1();
                push2(top2);
                push1(next);
                push2(top2);
            }
            case DUP2_X2 -> {
                var top2 = pop2();
                var next2 = pop2();
                push2(top2);
                push2(next2);
                push2(top2);
            }
            case SWAP -> {
                var top = pop1();
                var next = pop1();
                push1(top);
                push1(next);
            }
            default -> {
                assert false:MISSING + op;
            }
        }
    }

    @Override
    public void tableSwitch(Opcode op, TableSwitchInstruction inst) {
        popInt();
    }

    @Override
    public void throw_(Opcode op, ThrowInstruction inst) {
        popReference();
    }

    @Override
    public void typeCheck(Opcode op, TypeCheckInstruction inst) {
        popReference();
        switch(op) {
            case INSTANCEOF -> {
                pushInt();
            }
            case CHECKCAST -> {
                pushReference();
            }
            default -> {
                assert false:MISSING + op;
            }
        }
    }
    
}

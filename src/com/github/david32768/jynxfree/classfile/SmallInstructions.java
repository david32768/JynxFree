package com.github.david32768.jynxfree.classfile;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.lang.classfile.Instruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.david32768.jynxfree.my.Message.M603;
import static com.github.david32768.jynxfree.my.Message.M610;
import static com.github.david32768.jynxfree.my.Message.M626;
import static com.github.david32768.jynxfree.my.Message.M648;

import com.github.david32768.jynxfree.jynx.Global;
import com.github.david32768.jynxfree.jynx.LogIllegalArgumentException;

public class SmallInstructions {
    
    public static Instruction smallestSwitch(TableSwitchInstruction inst) {
        var cases =  sortCases(inst.opcode(), inst.lowValue(), inst.highValue(), inst.cases());
        return smallestSwitchImpl(inst.defaultTarget(), cases);
    }
    
    public static Instruction smallestSwitch(LookupSwitchInstruction inst) {
        var cases = sortCases(inst.opcode(), Integer.MIN_VALUE, Integer.MAX_VALUE, inst.cases());
        return smallestSwitchImpl(inst.defaultTarget(), cases);
    }
    
    public static Instruction smallestSwitch(Label deflab, List<SwitchCase> cases) {
        cases = sortCases(Opcode.LOOKUPSWITCH, Integer.MIN_VALUE, Integer.MAX_VALUE, cases);
        return smallestSwitchImpl(deflab, cases);
    }
    
    public static Instruction smallestSwitch(List<SwitchCase> cases) {
        if (cases.isEmpty()) {
            // "cannot calculate default label as no cases are present"
            throw new LogIllegalArgumentException(M648);
        }
        cases  = sortCases(Opcode.LOOKUPSWITCH, Integer.MIN_VALUE, Integer.MAX_VALUE, cases);
        Label lowtarget = cases.getFirst().target();
        Label hightarget = cases.getLast().target();

        var sw = smallestSwitchImpl(hightarget, cases);
        int sz = Instructions.sizeOfAt(sw, 0);

        var swL = smallestSwitchImpl(lowtarget, cases);
        int szL = Instructions.sizeOfAt(swL, 0);
        if (szL < sz) {
            sw = swL;
            sz = szL;
        }
        
        Map<Label, Long> uses = cases.stream()
                .collect(groupingBy(SwitchCase::target, counting()));
        var maxx = uses.entrySet().stream()
                .max((me1, me2) -> Long.compare(me1.getValue(), me2.getValue()))
                .get();
        Label maxtarget = maxx.getKey();
        if (maxx.getValue() > 1 && maxtarget != lowtarget && maxtarget != hightarget) {
            var swM = smallestSwitchImpl(maxtarget, cases);
            int szM = Instructions.sizeOfAt(swM, 0);
            if (szM < sz) {
                sw = swM;
            }
        }
        
        return sw;
    }
    
    private static Instruction smallestSwitchImpl(Label deflab, List<SwitchCase> casesx) {

        var cases = casesx.stream()
                .filter(swc -> swc.target() != deflab)
                .toList();
        
        var lookinst = LookupSwitchInstruction.of(deflab, cases);
        if (lookinst.cases().isEmpty()) {
            return lookinst;
        }
        long looksize = Instructions.getLookupSwitchSize(cases, 0);
        int low = cases.getFirst().caseValue();
        int high = cases.getLast().caseValue();
        long tabsize = Instructions.getTableSwitchSize(low, high, 0);
        return  tabsize < looksize?
                TableSwitchInstruction.of(low, high, deflab, cases):
                lookinst;
    }
    
    private static List<SwitchCase> sortCases(Opcode opcode, int low, int high, List<SwitchCase> cases) {
        if (cases.isEmpty()) {
            return cases;
        }
        var temp = new ArrayList<>(cases);
        Collections.sort(temp, (case1, case2) -> Integer.compare(case1.caseValue(), case2.caseValue()));
        List<SwitchCase> result = new ArrayList<>(temp.size());
        long last = Long.MIN_VALUE;
        Label lasttarget = null;
        for (var switchcase : temp) {
            long current = switchcase.caseValue();
            Label currenttarget = switchcase.target();
            if (current < low || current > high) {
                // "key %d is not in range [%d,%d]",
                Global.LOG(M626, current, low, high);
            } else if (current == last && currenttarget == lasttarget) {
                // "duplicate case %d in %s dropped"
                Global.LOG(M603, current, opcode);
            } else if (current == last) {
                // "ambiguous case %d in %s dropped"
                Global.LOG(M610, current, opcode);
            } else {
                result.add(switchcase);
            }
            last = current;
            lasttarget = currenttarget;
        }
        return result;
    }
    
    private final static List<Opcode> SMALL_INT = List.of(
            Opcode.ICONST_0,
            Opcode.ICONST_1,
            Opcode.ICONST_2,
            Opcode.ICONST_3,
            Opcode.ICONST_4,
            Opcode.ICONST_5);
    
    public static Instruction smallConst(ConstantInstruction coninst) {
        var desc = coninst.constantValue();
        return switch(desc) {
            case Integer ic -> {
                int value = ic;
                if (value == -1) {
                    yield CodeBuilderUtility.instructionOf(Opcode.ICONST_M1);
                } else if (value >= 0 && value < SMALL_INT.size()) {
                    yield CodeBuilderUtility.instructionOf(SMALL_INT.get(value));
                } else if (value == (byte)value) {
                   yield CodeBuilderUtility.instructionOf(Opcode.BIPUSH, value);
                } else if (coninst.opcode() == Opcode.LDC_W && value == (short)value) {
                    yield CodeBuilderUtility.instructionOf(Opcode.SIPUSH, value);
                } else {
                    yield coninst;
                }
            }
            case Long lc -> {
                long value = lc;
                if (value == 0) {
                    yield CodeBuilderUtility.instructionOf(Opcode.LCONST_0);
                } else if (value == 1) {
                    yield CodeBuilderUtility.instructionOf(Opcode.LCONST_1);
                } else {
                    yield coninst;
                }
            }
            case Float fc -> {
                float value = fc;
                if (Float.compare(value, +0.0F) == 0) { // not -0.0F
                    yield CodeBuilderUtility.instructionOf(Opcode.FCONST_0);
                } else if (value == 1.0F) {
                    yield CodeBuilderUtility.instructionOf(Opcode.FCONST_1);
                } else if (value == 2.0F) {
                    yield CodeBuilderUtility.instructionOf(Opcode.FCONST_2);
                } else {
                    yield coninst;
                }
            }
            case Double dc -> {
                double value = dc;
                if (Double.compare(value, +0.0) == 0) { // not -0.0
                    yield CodeBuilderUtility.instructionOf(Opcode.DCONST_0);
                } else if (value == 1.0) {
                    yield CodeBuilderUtility.instructionOf(Opcode.DCONST_1);
                } else {
                    yield coninst;
                }
            }
            default -> coninst;
        };
    }
    
}

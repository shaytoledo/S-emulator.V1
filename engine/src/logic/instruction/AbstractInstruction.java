package logic.instruction;

import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final Label label;
    private final Variable variable;
    public final Map<String,String> argsMap;
    public boolean basic = false;


    public AbstractInstruction(InstructionData instructionData, Variable variable, Map<String,String> argsMap ) {
        this(instructionData, variable, FixedLabel.EMPTY, argsMap);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label,  Map<String,String> argsMap) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;
        this.argsMap = Map.copyOf(argsMap);

    }


    public String arg(String key)    { return this.argsMap.get(key); }

    public void setBasic(boolean isBasic) {
        this.basic = isBasic;
    }

    @Override
    public Map<String,String> args() { return this.argsMap; }

    @Override
    public String getName() {
        return instructionData.getName();
    }

    @Override
    public int cycles() {
        return instructionData.getCycles();
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean isBasic() {
        return basic;
    }
}




//    @Override
//    public Label execute(ExecutionContext context) {
//        throw new IllegalStateException(
//                "Synthetic instruction '" + name + "' must be expanded before execution"
//        );
//    }
//
//    @Override
//    public int cycles() {
//        return 1;
//    }
//
//
//    public void setLabel(Label label) { this.label = label; }
//
//    public String toDisplayString() {}
//
//
//
//


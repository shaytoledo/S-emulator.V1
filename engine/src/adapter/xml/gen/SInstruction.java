package adapter.xml.gen;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SInstruction {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "S-Label")
    private String label;

    @XmlElement(name = "S-Variable")
    private String variable;

    @XmlElementWrapper(name = "S-Instruction-Arguments")
    @XmlElement(name = "S-Instruction-Argument")
    private List<SInstructionArgument> arguments;

    public String getName() { return name; }
    public String getType() { return type; }
    public String getLabel() { return label; }
    public String getVariable() { return variable; }
    public List<SInstructionArgument> getArguments() { return arguments; }
}
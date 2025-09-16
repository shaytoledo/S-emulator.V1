package adapter.xml.generated;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "S-Program")
@XmlAccessorType(XmlAccessType.FIELD)
public class SProgram {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElementWrapper(name = "S-Instructions")
    @XmlElement(name = "S-Instruction")
    private List<SInstruction> instructions;

    @XmlElementWrapper(name = "S-SFunctions")
    @XmlElement(name = "S-Functions")
    protected List<SFunction> functions;

    public String getName() { return name; }
    public List<SInstruction> getInstructions() { return instructions; }
    public List <SFunction> getSFunctions() {
        return functions;
    }

}
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

    public String getName() { return name; }
    public List<SInstruction> getInstructions() { return instructions; }
}
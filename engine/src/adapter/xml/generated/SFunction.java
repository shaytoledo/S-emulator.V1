package adapter.xml.generated;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SFunction {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "user-string")
    private String userString;

    @XmlElementWrapper(name = "S-Instructions")
    @XmlElement(name = "S-Instruction")
    private List<SInstruction> sInstructions;

    public List<SInstruction> getSInstructions() {
        return sInstructions;
    }
    public String getUserString() {
        return userString;
    }
    public String getName() {
        return name;
    }

}
package adapter.xml.generated;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sInstructions"
})
@XmlRootElement(name = "S-Function")
public class SFunction {

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlAttribute(name = "user-string", required = true)
    protected String userString;

    @XmlElement(name = "S-Instructions", required = true)
    protected List<SInstruction> sInstructions;

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
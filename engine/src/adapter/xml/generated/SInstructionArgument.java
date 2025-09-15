package adapter.xml.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SInstructionArgument {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "value")
    private String value;

    public String getName() { return name; }
    public String getValue() { return value; }
}
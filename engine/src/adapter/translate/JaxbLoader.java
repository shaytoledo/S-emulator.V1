package adapter.translate;

import jakarta.xml.bind.JAXBContext;
import adapter.xml.gen.SProgram;

import java.nio.file.Path;

// Loader for SProgram XML files using JAXB
public class JaxbLoader {

    public static SProgram load(Path xmlPath) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
        return (SProgram) jaxbContext.createUnmarshaller().unmarshal(xmlPath.toFile());
    }
}
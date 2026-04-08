package adapter.translate;

import adapter.xml.generated.SProgram;
import jakarta.xml.bind.JAXBContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

// Loader for SProgram XML files using JAXB
public class JaxbLoader {

    public static SProgram load(Path xmlPath) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
        return (SProgram) jaxbContext.createUnmarshaller().unmarshal(xmlPath.toFile());
    }

    public static SProgram loadFromStream(InputStream stream) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SProgram.class);
        return (SProgram) jaxbContext.createUnmarshaller().unmarshal(stream);
    }

    public static SProgram loadFromContent(String xmlContent) throws Exception {
        byte[] bytes = xmlContent.getBytes(StandardCharsets.UTF_8);
        return loadFromStream(new ByteArrayInputStream(bytes));
    }
}
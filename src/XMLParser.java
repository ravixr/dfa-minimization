import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLParser {

    public void parseXML(String filePath) {
        try {
            // Pega o caminho geral do programa
            System.out.println("Caminho do programa: " + System.getProperty("user.dir"));
            filePath = System.getProperty("user.dir") + "/src/" + filePath;
            XMLInputFactory factory = XMLInputFactory.newInstance();
            InputStream inputStream = new FileInputStream(new File(filePath));
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        System.out.println("Start Element: " + reader.getLocalName());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        System.out.println("End Element: " + reader.getLocalName());
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String text = reader.getText().trim();
                        if (!text.isEmpty()) {
                            System.out.println("Text: " + text);
                        }
                        break;
                    default:
                        break;
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

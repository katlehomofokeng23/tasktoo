import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XMLReader {
    public static void main(String[] args) {
        try {
            // Load the XML file
            File inputFile = new File("Task 2.xml");
            
            // Create a DocumentBuilder
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            // Parse the XML file
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            
            // Get all record elements
            NodeList recordList = doc.getElementsByTagName("record");
            
            System.out.println("Total records: " + recordList.getLength());
            System.out.println("----------------------------------------");
            
            // Iterate through each record
            for (int i = 0; i < recordList.getLength(); i++) {
                Node recordNode = recordList.item(i);
                
                if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element recordElement = (Element) recordNode;
                    
                    // Extract and print each field
                    System.out.println("Record #" + (i+1));
                    System.out.println("Name: " + getElementValue(recordElement, "name"));
                    System.out.println("PostalZip: " + getElementValue(recordElement, "postalZip"));
                    System.out.println("Region: " + getElementValue(recordElement, "region"));
                    System.out.println("Country: " + getElementValue(recordElement, "country"));
                    System.out.println("Address: " + getElementValue(recordElement, "address"));
                    System.out.println("List: " + getElementValue(recordElement, "list"));
                    System.out.println("----------------------------------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to get element value
    private static String getElementValue(Element parentElement, String elementName) {
        NodeList nodeList = parentElement.getElementsByTagName(elementName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return ""; // Return empty string if element not found
    }
}
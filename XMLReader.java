import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;

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
            
            // Get all available field names from the first record
            Set<String> availableFields = getAvailableFields(doc);
            
            // Get user input for field selection
            Set<String> selectedFields = getUserSelectedFields(availableFields);
            
            // Get all record elements
            NodeList recordList = doc.getElementsByTagName("record");
            
            // Start JSON output
            System.out.println("[");
            
            // Iterate through each record
            for (int i = 0; i < recordList.getLength(); i++) {
                Node recordNode = recordList.item(i);
                
                if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element recordElement = (Element) recordNode;
                    
                    // Start record JSON object
                    System.out.println("  {");
                    
                    // Print selected fields
                    boolean firstField = true;
                    for (String field : selectedFields) {
                        if (!firstField) {
                            System.out.println(",");
                        }
                        String value = getElementValue(recordElement, field);
                        System.out.print("    \"" + field + "\": " + formatJsonValue(value));
                        firstField = false;
                    }
                    
                    // End record JSON object
                    System.out.println();
                    System.out.print("  }");
                    
                    // Add comma unless it's the last record
                    if (i < recordList.getLength() - 1) {
                        System.out.println(",");
                    } else {
                        System.out.println();
                    }
                }
            }
            
            // End JSON output
            System.out.println("]");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Get all available field names from the first record
    private static Set<String> getAvailableFields(Document doc) {
        Set<String> fields = new LinkedHashSet<>(); // Maintain insertion order
        NodeList firstRecord = doc.getElementsByTagName("record");
        
        if (firstRecord.getLength() > 0) {
            Node recordNode = firstRecord.item(0);
            if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                Element recordElement = (Element) recordNode;
                NodeList childNodes = recordElement.getChildNodes();
                
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        fields.add(childNode.getNodeName());
                    }
                }
            }
        }
        return fields;
    }
    
    // Get user input for field selection
    private static Set<String> getUserSelectedFields(Set<String> availableFields) {
        Scanner scanner = new Scanner(System.in);
        Set<String> selectedFields = new LinkedHashSet<>(); // Maintain insertion order
        
        System.out.println("Available fields: " + String.join(", ", availableFields));
        System.out.println("Enter the fields you want to display (comma separated):");
        System.out.print("> ");
        
        String input = scanner.nextLine().trim();
        String[] choices = input.split(",");
        
        for (String choice : choices) {
            String field = choice.trim().toLowerCase();
            // Check if the field exists (case insensitive)
            for (String availableField : availableFields) {
                if (availableField.equalsIgnoreCase(field)) {
                    selectedFields.add(availableField);
                    break;
                }
            }
        }
        
        scanner.close();
        
        if (selectedFields.isEmpty()) {
            System.out.println("No valid fields selected. Showing all fields by default.");
            return availableFields;
        }
        
        return selectedFields;
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
    
    // Format value for JSON output (handle strings, numbers, and empty lists)
    private static String formatJsonValue(String value) {
        if (value.isEmpty()) {
            return "null";
        }
        
        // Check if it's a list of numbers
        if (value.matches("^\\s*\\d+(\\s*,\\s*\\d+)*\\s*$")) {
            return "[" + value + "]";
        }
        
        // Escape special JSON characters in strings
        value = value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        
        return "\"" + value + "\"";
    }
}
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.Scanner;
import java.util.LinkedHashSet;
import java.util.Set;

public class XMLReader {
    public static void main(String[] args) {
        try {
            // Validate XML file exists
            File inputFile = new File("Task 2.xml");
            if (!inputFile.exists()) {
                System.err.println("Error: Input file 'Task 2.xml' not found.");
                return;
            }

            // Parse XML with error handling
            Document doc;
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(inputFile);
                doc.getDocumentElement().normalize();
            } catch (Exception e) {
                System.err.println("Error parsing XML file: " + e.getMessage());
                return;
            }

            // Get available fields with validation
            Set<String> availableFields = getAvailableFields(doc);
            if (availableFields.isEmpty()) {
                System.err.println("Error: No valid fields found in XML.");
                return;
            }

            // Get user input with validation
            Set<String> selectedFields = getUserSelectedFields(availableFields);

            // Convert to JSON
            convertToJson(doc, selectedFields);

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private static Set<String> getAvailableFields(Document doc) {
        Set<String> fields = new LinkedHashSet<>();
        try {
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
        } catch (Exception e) {
            System.err.println("Warning: Error reading fields from XML - " + e.getMessage());
        }
        return fields;
    }

    private static Set<String> getUserSelectedFields(Set<String> availableFields) {
        Scanner scanner = new Scanner(System.in);
        Set<String> selectedFields = new LinkedHashSet<>();
        
        while (true) {
            try {
                System.out.println("\nAvailable fields: " + String.join(", ", availableFields));
                System.out.println("Enter the fields you want to display (comma separated, or 'all' for all fields):");
                System.out.print("> ");
                
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("all")) {
                    return availableFields;
                }
                
                if (input.isEmpty()) {
                    System.out.println("Please enter at least one field.");
                    continue;
                }
                
                String[] choices = input.split(",");
                boolean validSelection = false;
                
                for (String choice : choices) {
                    String field = choice.trim();
                    if (field.isEmpty()) continue;
                    
                    boolean found = false;
                    for (String availableField : availableFields) {
                        if (availableField.equalsIgnoreCase(field)) {
                            selectedFields.add(availableField);
                            found = true;
                            validSelection = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        System.out.println("Warning: Field '" + field + "' not found. Ignoring.");
                    }
                }
                
                if (validSelection) {
                    break;
                } else {
                    System.out.println("No valid fields selected. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
        
        return selectedFields;
    }

    private static void convertToJson(Document doc, Set<String> selectedFields) {
        try {
            NodeList recordList = doc.getElementsByTagName("record");
            if (recordList.getLength() == 0) {
                System.out.println("[]");
                return;
            }
            
            System.out.println("[");
            
            for (int i = 0; i < recordList.getLength(); i++) {
                Node recordNode = recordList.item(i);
                
                if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element recordElement = (Element) recordNode;
                    
                    System.out.println("  {");
                    
                    boolean firstField = true;
                    for (String field : selectedFields) {
                        if (!firstField) {
                            System.out.println(",");
                        }
                        String value = getSafeElementValue(recordElement, field);
                        System.out.print("    \"" + escapeJson(field) + "\": " + formatJsonValue(value));
                        firstField = false;
                    }
                    
                    System.out.println();
                    System.out.print("  }");
                    
                    if (i < recordList.getLength() - 1) {
                        System.out.println(",");
                    } else {
                        System.out.println();
                    }
                }
            }
            
            System.out.println("]");
            
        } catch (Exception e) {
            System.err.println("Error generating JSON: " + e.getMessage());
        }
    }

    private static String getSafeElementValue(Element parentElement, String elementName) {
        try {
            NodeList nodeList = parentElement.getElementsByTagName(elementName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                return node.getTextContent();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String formatJsonValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "null";
        }
        
        // Handle numeric arrays
        if (value.matches("^\\s*\\d+(\\s*,\\s*\\d+)*\\s*$")) {
            return "[" + value + "]";
        }
        
        return "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
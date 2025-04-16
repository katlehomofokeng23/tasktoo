import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.Scanner;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

public class XMLReader {
    private static Set<String> selectedFields;
    private static boolean isFirstRecord = true;
    private static StringBuilder jsonOutput = new StringBuilder();
    private static Stack<String> elementStack = new Stack<>();
    private static StringBuilder currentValue = new StringBuilder();

    public static void main(String[] args) {
        try {
            // Validate XML file exists
            File inputFile = new File("Task 2.xml");
            if (!inputFile.exists()) {
                System.err.println("Error: Input file 'Task 2.xml' not found.");
                return;
            }

            // First parse to get available fields
            Set<String> availableFields = getAvailableFields(inputFile);
            if (availableFields.isEmpty()) {
                System.err.println("Error: No valid fields found in XML.");
                return;
            }

            // Get user input with validation
            selectedFields = getUserSelectedFields(availableFields);

            // Initialize JSON output
            jsonOutput.append("[\n");

            // Parse with SAX
            parseXMLWithSAX(inputFile);

            // Complete JSON output
            jsonOutput.append("]");
            System.out.println(jsonOutput.toString());

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private static Set<String> getAvailableFields(File inputFile) throws Exception {
        Set<String> fields = new LinkedHashSet<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            private boolean firstRecordProcessed = false;

            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (qName.equalsIgnoreCase("record") && !firstRecordProcessed) {
                    firstRecordProcessed = true;
                }
            }

            public void endElement(String uri, String localName, String qName) {
                if (firstRecordProcessed && !qName.equalsIgnoreCase("record")) {
                    fields.add(qName);
                }
                if (qName.equalsIgnoreCase("record")) {
                    firstRecordProcessed = false; // Stop after first record
                }
            }
        };

        saxParser.parse(inputFile, handler);
        return fields;
    }

    private static Set<String> getUserSelectedFields(Set<String> availableFields) {
        Scanner scanner = new Scanner(System.in);
        Set<String> selected = new LinkedHashSet<>();
        
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
                            selected.add(availableField);
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
        
        return selected;
    }

    private static void parseXMLWithSAX(File inputFile) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            private boolean inSelectedField = false;
            private String currentField = null;
            private boolean inRecord = false;
            private boolean recordHasData = false;

            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                elementStack.push(qName);
                currentValue.setLength(0);

                if (qName.equalsIgnoreCase("record")) {
                    if (isFirstRecord) {
                        isFirstRecord = false;
                    } else if (recordHasData) {
                        jsonOutput.append(",\n");
                    }
                    jsonOutput.append("  {\n");
                    inRecord = true;
                    recordHasData = false;
                } else if (inRecord && selectedFields.contains(qName)) {
                    inSelectedField = true;
                    currentField = qName;
                }
            }

            public void characters(char[] ch, int start, int length) {
                if (inSelectedField) {
                    currentValue.append(ch, start, length);
                }
            }

            public void endElement(String uri, String localName, String qName) {
                if (inRecord && selectedFields.contains(qName) && currentValue.length() > 0) {
                    if (recordHasData) {
                        jsonOutput.append(",\n");
                    }
                    String value = currentValue.toString().trim();
                    jsonOutput.append("    \"").append(escapeJson(qName)).append("\": ")
                              .append(formatJsonValue(value));
                    recordHasData = true;
                }

                if (qName.equalsIgnoreCase("record") && inRecord) {
                    jsonOutput.append("\n  }");
                    inRecord = false;
                }

                elementStack.pop();
                inSelectedField = false;
                currentField = null;
            }
        };

        saxParser.parse(inputFile, handler);
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
import java.util.*;

public class Pass2 {

    private Map<String, Symbol> symtab;
    private List<Literal> littab;
    private List<String> intermediate;

    private List<String> objectCodes = new ArrayList<>();

    private String programName;
    private int startAddress;
    private int programLength;


    // Constructor
    public Pass2(
            Map<String, Symbol> symtab,
            List<Literal> littab,
            List<String> intermediate,
            int startAddress,
            int programLength) {

        this.symtab = symtab;
        this.littab = littab;
        this.intermediate = intermediate;
        this.startAddress = startAddress;
        this.programLength = programLength;

        // First symbol is the program name
        if (!symtab.isEmpty()) {
            programName = symtab.values()
                    .iterator()
                    .next()
                    .getName();
        }
    }


    // =========================
    // PASS 2
    // =========================

    public void run() {

        for (String line : intermediate) {

            String[] parts = line.split("\\s+");

            if (parts.length < 2) {
                continue;
            }

            String address = parts[0];

            String opcode;
            String operand = "";


            // Determine whether the line has a label

            if (parts.length >= 4) {

                // Example:
                // 6000 LITPRG START 6000

                opcode = parts[2];
                operand = parts[3];

            } else if (parts.length == 3) {

                // Example:
                // 6000 LDA =X'05'

                opcode = parts[1];
                operand = parts[2];

            } else {

                // Example:
                // 6009 RSUB

                opcode = parts[1];
            }


            // Ignore START and END

            if (opcode.equals("START") ||
                    opcode.equals("END")) {

                continue;
            }


            // =========================
            // MACHINE INSTRUCTION
            // =========================

            if (OpcodeTable.contains(opcode)) {

                String machineOpcode =
                        OpcodeTable.getOpcode(opcode);

                String operandAddress = "0000";


                if (!operand.isEmpty()) {

                    // Literal operand

                    if (operand.startsWith("=")) {

                        for (Literal literal : littab) {

                            if (literal.getLiteral()
                                    .equals(operand)) {

                                operandAddress =
                                        literal.getAddress();

                                break;
                            }
                        }
                    }

                    // Symbol operand

                    else {

                        if (symtab.containsKey(operand)) {

                            operandAddress =
                                    symtab.get(operand)
                                            .getAddress();
                        }
                    }
                }


                String objectCode =
                        machineOpcode + operandAddress;

                objectCodes.add(
                        address + "\t" + objectCode
                );
            }


            // =========================
            // BYTE DIRECTIVE
            // =========================

            else if (opcode.equals("BYTE")) {

                String value = operand;

                if (value.startsWith("C")) {

                    String chars =
                            value.substring(
                                    2,
                                    value.length() - 1
                            );

                    StringBuilder hex =
                            new StringBuilder();

                    for (char c : chars.toCharArray()) {

                        hex.append(
                                String.format(
                                        "%02X",
                                        (int) c
                                )
                        );
                    }

                    objectCodes.add(
                            address + "\t" + hex
                    );
                }

                else if (value.startsWith("X")) {

                    String hex =
                            value.substring(
                                    2,
                                    value.length() - 1
                            );

                    objectCodes.add(
                            address + "\t" + hex
                    );
                }
            }
        }


        // =========================
        // LITERAL OBJECT CODE
        // =========================

        for (Literal literal : littab) {

            objectCodes.add(
                    literal.getAddress()
                            + "\t"
                            + literal.getValue()
            );
        }
    }


    // =========================
    // DISPLAY OBJECT CODE
    // =========================

    public void displayObjectCode() {

        System.out.println(
                "\n========== PASS 2 ==========\n"
        );

        System.out.println("OBJECT CODE");
        System.out.println("--------------------");

        for (String code : objectCodes) {

            System.out.println(code);
        }
    }


    // =========================
    // GENERATE H/T/E RECORDS
    // =========================

    public void generateObjectProgram() {

        System.out.println(
                "\n========== OBJECT PROGRAM ==========\n"
        );


        // -------------------------
        // HEADER RECORD
        // -------------------------

        String header = String.format(
                "H^%-6s^%06X^%06X",
                programName,
                startAddress,
                programLength
        );

        System.out.println(header);


        // -------------------------
        // TEXT RECORDS
        // -------------------------

        StringBuilder textObjectCode =
                new StringBuilder();

        int textStartAddress = -1;
        int textLength = 0;


        for (String entry : objectCodes) {

            String[] parts =
                    entry.split("\\s+");

            int address =
                    Integer.parseInt(
                            parts[0],
                            16
                    );

            String code = parts[1];

            int codeLength =
                    code.length() / 2;


            // Start first text record

            if (textStartAddress == -1) {

                textStartAddress = address;
                textLength = 0;

                textObjectCode.setLength(0);
            }


            // Maximum SIC text record = 30 bytes

            if (textLength + codeLength > 30) {

                printTextRecord(
                        textStartAddress,
                        textLength,
                        textObjectCode.toString()
                );

                textStartAddress = address;
                textLength = 0;

                textObjectCode.setLength(0);
            }


            // Check for address gap

            if (textLength > 0) {

                int expectedAddress =
                        textStartAddress + textLength;

                if (address != expectedAddress) {

                    printTextRecord(
                            textStartAddress,
                            textLength,
                            textObjectCode.toString()
                    );

                    textStartAddress = address;
                    textLength = 0;

                    textObjectCode.setLength(0);
                }
            }


            textObjectCode.append(code);

            textLength += codeLength;
        }


        // Print final text record

        if (textLength > 0) {

            printTextRecord(
                    textStartAddress,
                    textLength,
                    textObjectCode.toString()
            );
        }


        // -------------------------
        // END RECORD
        // -------------------------

        String end = String.format(
                "E^%06X",
                startAddress
        );

        System.out.println(end);
    }


    // =========================
    // PRINT TEXT RECORD
    // =========================

    private void printTextRecord(
            int startAddress,
            int length,
            String objectCode) {

        System.out.printf(
                "T^%06X^%02X^%s%n",
                startAddress,
                length,
                objectCode
        );
    }


    // =========================
    // GET OBJECT CODES
    // =========================

    public List<String> getObjectCodes() {

        return objectCodes;
    }
}
import java.util.*;

public class Pass2 {

    private String programName = "";
    private Map<String, Symbol> symtab;
    private List<Literal> littab;
    private List<String> intermediate;

    private List<String> objectCodes = new ArrayList<>();
    private List<String> pass2Details = new ArrayList<>();
    private List<String> literalPoolDetails = new ArrayList<>();

    private int startAddress;
    private int programLength;
    private String endOperand = "";

    // Constructor with explicit program name
    public Pass2(
            String programName,
            Map<String, Symbol> symtab,
            List<Literal> littab,
            List<String> intermediate,
            int startAddress,
            int programLength) {

        this.programName = programName;
        this.symtab = symtab;
        this.littab = littab;
        this.intermediate = intermediate;
        this.startAddress = startAddress;
        this.programLength = programLength;
    }

    // Overloaded Constructor for backwards compatibility
    public Pass2(
            Map<String, Symbol> symtab,
            List<Literal> littab,
            List<String> intermediate,
            int startAddress,
            int programLength) {

        this("", symtab, littab, intermediate, startAddress, programLength);

        if (!symtab.isEmpty()) {
            this.programName = symtab.values()
                    .iterator()
                    .next()
                    .getName();
        }
    }

    private boolean isDirective(String s) {
        return s.equals("START") || s.equals("END") || s.equals("WORD")
                || s.equals("RESW") || s.equals("RESB") || s.equals("BYTE")
                || s.equals("LTORG");
    }

    // =========================
    // PASS 2
    // =========================

    public void run() {

        Set<String> processedLiterals = new HashSet<>();

        for (String line : intermediate) {

            String[] parts = line.split("\\s+");

            if (parts.length < 2) {
                continue;
            }

            String address = parts[0];

            // Case 1: Literal line from intermediate file
            // Example: "600F * =X'05'"
            if (parts[1].equals("*")) {

                String litName = parts.length > 2 ? parts[2] : "";

                for (Literal literal : littab) {

                    if (literal.getLiteral().equals(litName)
                            && !processedLiterals.contains(litName)) {

                        processedLiterals.add(litName);

                        objectCodes.add(
                                address + "\t" + literal.getValue()
                        );

                        literalPoolDetails.add(
                                address + "\t"
                                        + litName + "\t"
                                        + literal.getValue()
                        );

                        break;
                    }
                }

                continue;
            }

            // Case 2: Instruction or directive
            String label = "";
            String opcode;
            String operand = "";

            if (OpcodeTable.contains(parts[1])
                    || isDirective(parts[1])) {

                opcode = parts[1];

                if (parts.length > 2) {
                    operand = parts[2];
                }

            } else {

                label = parts[1];

                opcode = parts.length > 2
                        ? parts[2]
                        : "";

                if (parts.length > 3) {
                    operand = parts[3];
                }
            }

            // Handle START and END
            if (opcode.equals("START")) {

                if (programName == null
                        || programName.isEmpty()) {

                    programName = label;
                }

                continue;
            }

            if (opcode.equals("END")) {

                endOperand = operand;

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

                String instruction =
                        (label.isEmpty()
                                ? ""
                                : label + " ")
                                + opcode
                                + (operand.isEmpty()
                                ? ""
                                : " " + operand);

                pass2Details.add(
                        address + "\t"
                                + instruction + "\t"
                                + objectCode
                );
            }

            // =========================
            // WORD DIRECTIVE
            // =========================

            else if (opcode.equals("WORD")) {

                int val =
                        Integer.parseInt(operand);

                String objectCode =
                        String.format(
                                "%06X",
                                val & 0xFFFFFF
                        );

                objectCodes.add(
                        address + "\t" + objectCode
                );

                String instruction =
                        (label.isEmpty()
                                ? ""
                                : label + " ")
                                + opcode + " " + operand;

                pass2Details.add(
                        address + "\t"
                                + instruction + "\t"
                                + objectCode
                );
            }

            // =========================
            // BYTE DIRECTIVE
            // =========================

            else if (opcode.equals("BYTE")) {

                String hex = "";

                if (operand.startsWith("C")) {

                    String chars =
                            operand.substring(
                                    2,
                                    operand.length() - 1
                            );

                    StringBuilder sb =
                            new StringBuilder();

                    for (char c : chars.toCharArray()) {

                        sb.append(
                                String.format(
                                        "%02X",
                                        (int) c
                                )
                        );
                    }

                    hex = sb.toString();

                } else if (operand.startsWith("X")) {

                    hex =
                            operand.substring(
                                    2,
                                    operand.length() - 1
                            );

                    if (hex.length() % 2 != 0) {
                        hex = "0" + hex;
                    }
                }

                objectCodes.add(
                        address + "\t" + hex
                );

                String instruction =
                        (label.isEmpty()
                                ? ""
                                : label + " ")
                                + opcode + " " + operand;

                pass2Details.add(
                        address + "\t"
                                + instruction + "\t"
                                + hex
                );
            }

            // =========================
            // RESW / RESB DIRECTIVES
            // =========================

            else if (opcode.equals("RESW")
                    || opcode.equals("RESB")) {

                // Storage reservations generate no object code
            }
        }

        // =========================
        // LITERAL OBJECT CODE (FALLBACK)
        // =========================

        for (Literal literal : littab) {

            if (!processedLiterals.contains(
                    literal.getLiteral())) {

                objectCodes.add(
                        literal.getAddress()
                                + "\t"
                                + literal.getValue()
                );

                literalPoolDetails.add(
                        literal.getAddress()
                                + "\t"
                                + literal.getLiteral()
                                + "\t"
                                + literal.getValue()
                );
            }
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

        System.out.println(
                "------------------------------------------------"
        );

        System.out.printf(
                "%-10s %-18s %-12s%n",
                "Address",
                "Instruction",
                "Object Code"
        );

        System.out.println(
                "------------------------------------------------"
        );

        for (String line : pass2Details) {

            String[] parts =
                    line.split("\\t", -1);

            String addr =
                    parts.length > 0
                            ? parts[0]
                            : "";

            String instr =
                    parts.length > 1
                            ? parts[1]
                            : "";

            String obj =
                    parts.length > 2
                            ? parts[2]
                            : "";

            System.out.printf(
                    "%-10s %-18s %-12s%n",
                    addr,
                    instr,
                    obj
            );
        }

        System.out.println(
                "------------------------------------------------"
        );

        System.out.println("\nLITERAL POOL");

        System.out.println(
                "------------------------------------------------"
        );

        for (String line : literalPoolDetails) {

            String[] parts =
                    line.split("\\t", -1);

            String addr =
                    parts.length > 0
                            ? parts[0]
                            : "";

            String lit =
                    parts.length > 1
                            ? parts[1]
                            : "";

            String obj =
                    parts.length > 2
                            ? parts[2]
                            : "";

            System.out.printf(
                    "%-10s %-18s %-12s%n",
                    addr,
                    lit,
                    obj
            );
        }

        System.out.println(
                "------------------------------------------------"
        );
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

        String header =
                String.format(
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

            // Add ^ between complete object codes
            if (textObjectCode.length() > 0) {
                textObjectCode.append("^");
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

        int firstExecAddr = startAddress;

        if (!endOperand.isEmpty()) {

            if (symtab.containsKey(endOperand)) {

                firstExecAddr =
                        Integer.parseInt(
                                symtab.get(endOperand)
                                        .getAddress(),
                                16
                        );

            } else if (endOperand.equals(programName)) {

                firstExecAddr = startAddress;
            }
        }

        String end =
                String.format(
                        "E^%06X",
                        firstExecAddr
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
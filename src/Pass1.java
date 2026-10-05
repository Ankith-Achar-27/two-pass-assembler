import java.util.*;
import java.io.*;

public class Pass1 {

    private String programName = "";
    private Map<String, Symbol> symtab = new LinkedHashMap<>();
    private List<Literal> littab = new ArrayList<>();
    private List<String> intermediate = new ArrayList<>();

    private int locctr;
    private int startAddress;
    private int programLength;

    public void run(String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        boolean firstLine = true;

        while ((line = br.readLine()) != null) {

            line = line.trim();

            // Ignore empty lines
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");

            String label = "";
            String opcode;
            String operand = "";

            /*
             * Determine whether the first word is a label.
             */
            if (OpcodeTable.contains(parts[0])
                    || parts[0].equals("START")
                    || parts[0].equals("END")
                    || parts[0].equals("WORD")
                    || parts[0].equals("RESW")
                    || parts[0].equals("RESB")
                    || parts[0].equals("BYTE")
                    || parts[0].equals("LTORG")) {

                opcode = parts[0];

                if (parts.length > 1) {
                    operand = parts[1];
                }

            } else {

                label = parts[0];
                opcode = parts[1];

                if (parts.length > 2) {
                    operand = parts[2];
                }
            }

            /*
             * START directive
             */
            if (opcode.equals("START")) {

                startAddress = Integer.parseInt(operand, 16);
                locctr = startAddress;
                programName = label;

                // Add program name to SYMTAB
                if (!label.isEmpty()) {
                    symtab.put(
                            label,
                            new Symbol(
                                    label,
                                    String.format("%04X", locctr)
                            )
                    );
                }

                intermediate.add(
                        String.format("%04X\t%s", locctr, line)
                );

                firstLine = false;
                continue;
            }

            /*
             * Add label to SYMTAB (program name on START is not added)
             */
            if (!label.isEmpty()) {

                if (symtab.containsKey(label)) {
                    System.out.println("Error: Duplicate symbol " + label);
                } else {
                    symtab.put(
                            label,
                            new Symbol(label,
                                    String.format("%04X", locctr))
                    );
                }
            }

            /*
             * Add literal to LITTAB
             */
            if (operand.startsWith("=")) {

                boolean exists = false;

                for (Literal literal : littab) {
                    if (literal.getLiteral().equals(operand)) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {

                    String value;
                    int length;

                    if (operand.startsWith("=X")) {

                        value = operand.substring(3, operand.length() - 1);
                        if (value.length() % 2 != 0) {
                            value = "0" + value;
                        }
                        length = value.length() / 2;

                    } else if (operand.startsWith("=C")) {

                        String chars = operand.substring(3, operand.length() - 1);
                        StringBuilder hex = new StringBuilder();
                        for (char c : chars.toCharArray()) {
                            hex.append(String.format("%02X", (int) c));
                        }
                        value = hex.toString();
                        length = chars.length();

                    } else {
                        value = "";
                        length = 0;
                    }

                    littab.add(
                            new Literal(
                                    operand,
                                    value,
                                    length
                            )
                    );
                }
            }

            /*
             * Save intermediate line
             */
            intermediate.add(
                    String.format("%04X\t%s", locctr, line)
            );

            /*
             * Update LOCCTR
             */
            if (OpcodeTable.contains(opcode)) {

                locctr += 3;

            } else if (opcode.equals("WORD")) {

                locctr += 3;

            } else if (opcode.equals("RESW")) {

                locctr += 3 * Integer.parseInt(operand);

            } else if (opcode.equals("RESB")) {

                locctr += Integer.parseInt(operand);

            } else if (opcode.equals("BYTE")) {

                if (operand.startsWith("C")) {

                    int length = operand.substring(2,
                            operand.length() - 1).length();

                    locctr += length;

                } else if (operand.startsWith("X")) {

                    int length = operand.substring(2,
                            operand.length() - 1).length();

                    locctr += (length + 1) / 2;
                }

            } else if (opcode.equals("LTORG") || opcode.equals("END")) {

                /*
                 * Assign addresses to literals and append to intermediate file
                 */
                for (Literal literal : littab) {

                    if (literal.getAddress() == null) {

                        literal.setAddress(
                                String.format("%04X", locctr)
                        );

                        intermediate.add(
                                String.format("%04X\t*\t%s", locctr, literal.getLiteral())
                        );

                        locctr += literal.getLength();
                    }
                }

                if (opcode.equals("END")) {
                    break;
                }
            }
        }

        br.close();

        programLength = locctr - startAddress;
    }

    public void displayResults() {

        System.out.println("\n========== PASS 1 ==========\n");

        System.out.println("SYMBOL TABLE");
        System.out.println("--------------------");

        for (Symbol symbol : symtab.values()) {
            System.out.println(
                    symbol.getName() +
                            " -> " +
                            symbol.getAddress()
            );
        }

        System.out.println("\nLITERAL TABLE");
        System.out.println("--------------------");

        for (Literal literal : littab) {

            System.out.println(
                    literal.getLiteral() +
                            " -> " +
                            literal.getAddress()
            );
        }

        System.out.println("\nINTERMEDIATE FILE");
        System.out.println("--------------------");

        for (String line : intermediate) {
            System.out.println(line);
        }

        System.out.println("\nSTART ADDRESS : "
                + String.format("%04X", startAddress));

        System.out.println("PROGRAM LENGTH : "
                + String.format("%04X", programLength));
    }

    public String getProgramName() {
        return programName;
    }

    public Map<String, Symbol> getSymtab() {
        return symtab;
    }

    public List<Literal> getLittab() {
        return littab;
    }

    public List<String> getIntermediate() {
        return intermediate;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getProgramLength() {
        return programLength;
    }
}
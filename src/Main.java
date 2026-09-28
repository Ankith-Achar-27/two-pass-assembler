public class Main {

    public static void main(String[] args) {

        try {

            // =========================
            // PASS 1
            // =========================

            Pass1 pass1 = new Pass1();

            pass1.run("src/input.txt");

            pass1.displayResults();


            // =========================
            // PASS 2
            // =========================

            Pass2 pass2 = new Pass2(
                    pass1.getProgramName(),
                    pass1.getSymtab(),
                    pass1.getLittab(),
                    pass1.getIntermediate(),
                    pass1.getStartAddress(),
                    pass1.getProgramLength()
            );

            pass2.run();

            pass2.displayObjectCode();
            pass2.generateObjectProgram();

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }
}
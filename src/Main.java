/**
 * @authors https://github.com/ravixr/dfa-minimization
 */

class Main {

    // Usage: java Main <input_file> <method_number> (1 or 2)
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java Main <input_file> <method_number> (1 or 2)");
        }

        DFA dfa = DFA.FromJFLAPXML(args[0]);
        DFA min = null;
        if (Integer.parseInt(args[1]) == 1) {
            min = dfa.stdMinimization();
        } else if (Integer.parseInt(args[1]) == 2) {
            min = dfa.stdMinimization2();
        } else {
            throw new IllegalArgumentException("Method number must be either 1 or 2");
        }
        
        int lastDot = args[0].lastIndexOf('.');
        if (lastDot == -1) {
            lastDot = args[0].length();
        }
        String filename = args[0].substring(0, lastDot) + ".min.jff";
        min.SaveJFLAPXML(filename);

        System.out.println("Original DFA:" + dfa);
        System.out.println("Minimized DFA:" + min);
        System.out.println("Minimized version saved to " + filename);
    }
}
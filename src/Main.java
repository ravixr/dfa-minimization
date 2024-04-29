/**
 * @authors https://github.com/ravixr/dfa-minimization
 */

class Main {

    // Usage: java Main <input_file>
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java Main <input_file>");
        }

        DFA dfa = DFA.FromJFLAPXML(args[0]);
        DFA min = dfa.stdMinimization();

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
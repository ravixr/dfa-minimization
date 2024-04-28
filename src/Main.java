import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

class Main {
    public static void main(String[] args) throws Exception {

        // DFA test = DFA.FromJFLAPXML("std_test.jff");
        // DFA min = test.stdMinimization();
        // min.SaveJFLAPXML("std_test_min.jff");

        boolean[] isFinal = {false, false, true, false, true};
        DFA dfa = new DFA(5, List.of("0","1"), 0, isFinal);
        dfa.addTransition(0, 1, "0");
        dfa.addTransition(0, 3, "1");
        dfa.addTransition(1, 1, "0");
        dfa.addTransition(1, 2, "1");
        dfa.addTransition(2, 3, "0");
        dfa.addTransition(2, 4, "1");
        dfa.addTransition(3, 3, "0");
        dfa.addTransition(3, 4, "1");
        dfa.addTransition(4, 1, "0");
        dfa.addTransition(4, 2, "1");

        System.out.println(dfa.toString());
        System.out.println("\n-----------------------------------------------\n");
        dfa = dfa.stdMinimization2();

        System.out.println(dfa.toString());
    }
}
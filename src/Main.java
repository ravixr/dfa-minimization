import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Main {
    public static void main(String[] args) throws Exception {

        // DFA k = DFA.generateDoubleStateTest(1000);
        // k.SaveJFLAPXML("1k.jff");
        // DFA tenk = DFA.generateDoubleStateTest(10000);
        // tenk.SaveJFLAPXML("10k.jff");


        int[] sizes = { 10, 100, 1000 };
        int[] symbolSetSizes = { 2, 10, 20 };
        // { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t" };
        List<String> symbolSet = new ArrayList<String>();
        for (int i = 0; i < 26; i++) {
            symbolSet.add(Character.toString((char) (i + 97)));
        }

        System.out.println("inst,std_size,min1_size,min2_size,time1,time2");
        int count = 0;
        for (int i = 0; i < sizes.length; i++) {
            for (int j = 0; j < 5; j++) {
                //DFA gen = DFA.generateDFA(sizes[i], symbolSet.subList(0, symbolSetSizes[i]));
                DFA gen = DFA.generateDoubleStateTest(sizes[i]);
                long start1 = System.nanoTime();
                DFA min = gen.stdMinimization();
                long end1 = System.nanoTime();
                long time1 = end1 - start1;
                long start2 = System.nanoTime();
                DFA min2 = gen.stdMinimization2();
                long end2 = System.nanoTime();
                long time2 = end2 - start2;
                if (i == 0 && j == 0) {
                    gen.SaveJFLAPXML("std_test.jff");
                    min.SaveJFLAPXML("std_test_min.jff");
                    min2.SaveJFLAPXML("std_test_min2.jff");
                }
                System.out.println(count++ + "," + gen.adj.size() + "," + min.adj.size() + "," + min2.adj.size() + "," + time1 + ","
                        + time2);
            }
        }


        // DFA test = DFA.FromJFLAPXML("std_test.jff");
        // DFA min = test.stdMinimization();
        // min.SaveJFLAPXML("std_test_min.jff");

        // boolean[] isFinal = {false, false, true, false, true};
        // DFA dfa = new DFA(5, List.of("0","1"), 0, isFinal);
        // dfa.addTransition(0, 1, "0");
        // dfa.addTransition(0, 3, "1");
        // dfa.addTransition(1, 1, "0");
        // dfa.addTransition(1, 2, "1");
        // dfa.addTransition(2, 3, "0");
        // dfa.addTransition(2, 4, "1");
        // dfa.addTransition(3, 3, "0");
        // dfa.addTransition(3, 4, "1");
        // dfa.addTransition(4, 1, "0");
        // dfa.addTransition(4, 2, "1");

        // System.out.println(dfa.toString());
        // System.out.println("\n-----------------------------------------------\n");
        // dfa = dfa.stdMinimization2();

        // System.out.println(dfa.toString());

    }
}
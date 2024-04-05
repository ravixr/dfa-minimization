import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

class Transition {
    public int to;
    public char symbol;

    public Transition(int to, char symbol) {
        this.to = to;
        this.symbol = symbol;
    }
}

// Deterministic Finite Automaton
class DFA {
    // Adjacency list representation of the DFA
    List<List<Transition>> adj;
    int initialState;
    boolean[] finalStates;

    public DFA(int nStates, int initialState, boolean[] finalStates) {
        this.initialState = initialState;
        this.finalStates = finalStates;
        adj = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addTransition(int from, int to, char symbol) {
        adj.get(from).add(new Transition(to, symbol));
    }

    private static final String WATERMARK = "<!--Created by Bogosort (\'-\')-->\n";

    /**
     * @return A string containing a JFLAP 7.0 XML compatible description of this automaton.
     */
    public String toJFLAPXML() {
        String xml = "";
        xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
        xml += WATERMARK;
        xml += "<structure>&#13;\n";
        xml += "\t<type>fa</type>&#13;\n";
        xml += "\t<automaton>&#13;\n";
        // States
        for (int i = 0; i < adj.size(); i++) {
            xml += "\t\t<state id=\"" + i + "\" name=\"q" + i + "\">&#13;\n";
            xml += "\t\t\t<x>0</x>&#13;\n";
            xml += "\t\t\t<y>0</y>&#13;\n";
            if (i == initialState) {
                xml += "\t\t\t<initial/>&#13;\n";
            }
            if (finalStates[i]) {
                xml += "\t\t\t<final/>&#13;\n";
            }
            xml += "\t\t</state>&#13;\n";
        }
        // Transitions
        for (int i = 0; i < adj.size(); i++) {
            xml += "\t\t<transition>&#13;\n";
            for (Transition j : adj.get(i)) {
                xml += "\t\t\t<from>" + i + "</from>&#13;\n";
                xml += "\t\t\t<to>" + j.to + "</to>&#13;\n";
                xml += "\t\t\t<read>" + j.symbol + "</read>&#13;\n";
            }
            xml += "\t\t</transition>&#13;\n";
        }
        xml += "\t</automaton>&#13;\n";
        xml += "</structure>\n";
        return xml;
    }

    public static void foo() {
        DFA a = new DFA(3, 0, new boolean[]{true, false, false});
        a.addTransition(0, 0, '1');
        a.addTransition(0, 1, '0');
        a.addTransition(1, 2, '0');
        a.addTransition(2, 2, '0');

        System.out.println(a.toJFLAPXML());
    }
}
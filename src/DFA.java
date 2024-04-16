import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Transition {
    public int from;
    public int to;
    public String symbol;

    public Transition() {
    }
    public Transition(int to, String symbol) {
        this.to = to;
        this.symbol = symbol;
    }
}

class State {
    public int id;
    public String name;

    public State(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

// Deterministic Finite Automaton
class DFA {
    // Lista de transiçoes, cada transição é um hashmap de char -> int
    public List<HashMap<String, Integer>> adj;
    public int initialState;
    public List<State> states;
    public boolean[] finalStates;
    public List<String> symbolSet;

    public DFA(int nStates, List<String> symbolSet) {
        this.initialState = -1;
        this.states = new ArrayList<>();
        this.symbolSet = symbolSet;
        this.finalStates = new boolean[nStates];
        this.adj = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            this.adj.add(new HashMap<>());
        }
    }

    public DFA(int nStates, List<String> symbolSet, int initialState, boolean[] finalStates) {
        this.symbolSet = symbolSet;
        this.initialState = initialState;
        this.finalStates = finalStates;
        this.adj = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            this.adj.add(new HashMap<>());
        }
    }

    public void addTransition(int from, int to, String symbol) {
        adj.get(from).put(symbol, to);
    }

    @Override
    public DFA clone() {
        DFA newDfa = new DFA(adj.size(), symbolSet, initialState, finalStates);
        for (int i = 0; i < adj.size(); i++) {
            for (String symbol : adj.get(i).keySet()) {
                newDfa.addTransition(i, adj.get(i).get(symbol), symbol);
            }
        }
        return newDfa;
    }

    /**
     * Minimizes the DFA using the standard algorithm O(kn^2).
     * 
     * @return A minimized version of the DFA.
     */
    public DFA stdMinimization() {
        List<List<State>> Q = new ArrayList<>();
        int[] statePartition = new int[adj.size()];
        Q.add(new ArrayList<>());
        Q.add(new ArrayList<>());
        // Partition the states in final and non-final states
        for (int i = 0; i < adj.size(); i++) {
            if (finalStates[i]) {
                Q.get(1).add(new State(i, "q" + i));
                statePartition[i] = 1;
            } else {
                Q.get(0).add(new State(i, "q" + i));
                statePartition[i] = 0;
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < Q.size(); i++) {
                List<List<State>> newPartitions = partition(Q.get(i), statePartition);
                // If the partition was split adds the new partitions
                if (newPartitions.size() > 1) {
                    Q.remove(i);
                    for (List<State> partition : newPartitions) {
                        Q.add(partition);
                    }
                    // Update state partition map
                    for (int j = 0; j < Q.size(); j++) {
                        for (State state : Q.get(j)) {
                            statePartition[state.id] = j;
                        }
                    }
                    changed = true;
                }
            }
        }
        // Merge states in the same partition
        // TODO - Remove unreachable states?
        int n = Arrays.stream(statePartition).max().getAsInt() + 1;
        DFA min = new DFA(n, symbolSet);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < adj.size(); j++) {
                if (statePartition[j] == i) {
                    if (initialState == j) {
                        min.initialState = i;
                    } 
                    if (finalStates[j]) {
                        min.finalStates[i] = true;
                    }
                    for (String symbol : symbolSet) {
                        int to = adj.get(j).get(symbol);
                        min.adj.get(i).put(symbol, statePartition[to]);
                    }
                    break;
                }
            }
        }
        return min;
    }

    private List<List<State>> partition(List<State> partition, int[] statePartition) {
        List<List<State>> newPartitions = new ArrayList<>();
        if (partition.size() <= 1) {
            return newPartitions;
        }
        newPartitions.add(new ArrayList<>());
        for (int i = 0; i < newPartitions.size(); i++) {
            newPartitions.get(i).add(partition.get(0));
            partition.remove(0);
            for (int j = 0; j < partition.size(); j++) {
                boolean belongsToPartition = true;
                for (String symbol : symbolSet) {
                    int to1 = adj.get(partition.get(j).id).get(symbol);
                    int to2 = adj.get(newPartitions.get(i).get(0).id).get(symbol);
                    if (statePartition[to1] != statePartition[to2]) {
                        belongsToPartition = false;
                        break;
                    }
                }
                if (belongsToPartition) {
                    newPartitions.get(i).add(partition.get(j));
                    partition.remove(j);
                    j--;
                }
            }
            if (partition.size() > 0) {
                newPartitions.add(new ArrayList<>());
            }
        }
        for (int i = 0; i < newPartitions.size(); i++) {
            if (newPartitions.get(i).size() == 0) {
                newPartitions.remove(i);
                i--;
            }
        }

        return newPartitions;
    }

    private static final String WATERMARK = "<!-- Created by https://github.com/ravixr/dfa-minimization -->\n";

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
            
            for (String symbol : adj.get(i).keySet()) {
                xml += "\t\t<transition>&#13;\n";
                xml += "\t\t\t<from>" + i + "</from>&#13;\n";
                xml += "\t\t\t<to>" + adj.get(i).get(symbol) + "</to>&#13;\n";
                xml += "\t\t\t<read>" + symbol + "</read>&#13;\n";
                xml += "\t\t</transition>&#13;\n";
            }
            
        }
        xml += "\t</automaton>&#13;\n";
        xml += "</structure>\n";
        return xml;
    }

    public void SaveJFLAPXML(String filePath) {
        try {
            filePath = Paths.get("").toAbsolutePath().toString().split("/src")[0] + ("/tests/" + filePath);
            File file = new File(filePath);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(toJFLAPXML());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DFA FromJFLAPXML(String filePath) {
        DFA new_dfa = null;
        int nStates = 0;
        int initialState = 0;
        List<State> states = new ArrayList<>();
        List<Transition> transitions = new ArrayList<>();
        List<Integer> finalStatesList = new ArrayList<>();
        boolean[] finalStates = null;
        List<HashMap<Character, Integer>> adj = new ArrayList<>();
        List<String> symbolSet = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            adj.add(new HashMap<>());
        }
        try {
            

            

            // Pega o caminho geral do programa
            //System.out.println("Caminho do programa: " + System.getProperty("user.dir"));
            filePath = Paths.get("").toAbsolutePath().toString().split("/src")[0] + ("/tests/" + filePath);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            InputStream inputStream = new FileInputStream(new File(filePath));
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            String lastElement = "";
            Transition currentTransition = null;
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "state":
                                nStates++;
                                int id = Integer.parseInt(reader.getAttributeValue(null, "id"));
                                String name = reader.getAttributeValue(null, "name");
                                states.add(new State(id, name));
                                lastElement = "state";
                                break;
                            case "transition":
                                currentTransition = new Transition();
                                lastElement = "transition";
                                break;
                            case "to":
                                lastElement = "to";
                                break;
                            case "from":
                                lastElement = "from";
                                break;
                            case "read":
                                lastElement = "read";
                                break;
                            default:
                                break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "transition":
                                transitions.add(currentTransition);
                                break;
                            case "initial":
                                initialState = states.get(states.size() - 1).id;
                                break;
                            case "final":
                                finalStatesList.add(states.get(states.size() - 1).id);
                                break;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (!(lastElement.equals("read") || lastElement.equals("to") || lastElement.equals("from")) || reader.isWhiteSpace()){
                            break;
                        }
                        String text = reader.getText().trim();
                        switch (lastElement) {
                            case "to":
                                currentTransition.to = Integer.parseInt(text);
                                break;
                            case "from":
                                currentTransition.from = Integer.parseInt(text);
                                break;
                            case "read":
                                currentTransition.symbol = text;
                                if (!symbolSet.contains(text)) {
                                    symbolSet.add(text);
                                }
                                break;

                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        finalStates = new boolean[nStates];
        for (int i = 0; i < finalStatesList.size(); i++) {
            finalStates[finalStatesList.get(i)] = true;
        }

        new_dfa = new DFA(nStates, symbolSet, initialState, finalStates);
        for (Transition t : transitions) {
            new_dfa.addTransition(t.from, t.to, t.symbol);
        }

        return new_dfa;
    }

    @Override
    public String toString() {
        String str = "";
        str += "Initial state: " + initialState + "\n";
        str += "Final states: ";
        for (int i = 0; i < finalStates.length; i++) {
            if (finalStates[i]) {
                str += i + " ";
            }
        }
        str += "\n";
        for (int i = 0; i < adj.size(); i++) {
            for (String symbol : adj.get(i).keySet()) {
                str += "q" + i + " -> q" + adj.get(i).get(symbol) + " on " + symbol + "\n";
            }
        }

        return str;
    }

    public static void foo() {
        DFA test = DFA.FromJFLAPXML("std_test.jff");
        DFA min = test.stdMinimization();
        min.SaveJFLAPXML("std_test_min.jff");
    }
}
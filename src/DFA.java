/**
 * @authors https://github.com/ravixr/dfa-minimization
 */

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
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
import java.util.HashSet;

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
    @Override
    public String toString() {
        return String.format("State{id=%d, name='%s'}", id, name);
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
    
    public boolean addTransition(int from, int to, String symbol) {
        if ((from < 0 || from >= adj.size()) || (to < 0 || to >= adj.size()) || adj.get(from).containsKey(symbol)) {
            return false;
        }
        adj.get(from).put(symbol, to);
        return true;
    }

    public static DFA generateDFA(int nStates, List<String> symbolSet) {
        DFA dfa = new DFA(nStates, symbolSet);
        dfa.initialState = 0;
        Random r = new Random();
        int nFinalStates = 0;

        // Make sure all states are reachable
        List<Integer> connectedStates = new ArrayList<>();
        List<Integer> unconnectedStates = new ArrayList<>();
        for (int i = 1; i < nStates; i++) {
            unconnectedStates.add(i);
        }
        connectedStates.add(dfa.initialState);
        while (unconnectedStates.size() > 0) {
            int from = connectedStates.get(r.nextInt(connectedStates.size()));
            int to = unconnectedStates.get(r.nextInt(unconnectedStates.size()));
            String symbol = symbolSet.get(r.nextInt(symbolSet.size()));
            if (dfa.addTransition(from, to, symbol)) {
                connectedStates.add(to);
                unconnectedStates.remove(unconnectedStates.indexOf(to));
            }
        }
    
        // Add rest of transitions
        List<Integer> cadidateStates = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            for (int j = 0; j < (symbolSet.size() - dfa.adj.get(i).size()); j++)
                cadidateStates.add(i);
        }
        for (int i = 0; i < nStates; i++) {
            for (int j = 0; j < symbolSet.size(); j++) {
                int to = cadidateStates.get(r.nextInt(cadidateStates.size()));
                dfa.addTransition(i, to, symbolSet.get(j));
            }
        }

        // Ramdom DFS to mark some states as final
        boolean[] visited = new boolean[nStates];
        List<Integer> stack = new ArrayList<>();
        stack.add(dfa.initialState);
        while (stack.size() > 0) {
            int state = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
            visited[state] = true;
            if (r.nextInt(10) == 0) {
                dfa.finalStates[state] = true;
                nFinalStates++;
            }
            for (String symbol : symbolSet) {
                Integer to = dfa.adj.get(state).get(symbol);
                if (to != null && !visited[to]) {
                    stack.add(to);
                }
            }
            if (nFinalStates == 0 && stack.size() == 0) {
                dfa.finalStates[state] = true;
                nFinalStates++;
            }
        }

        for (int i = 0; i < nStates; i++) {
            if (dfa.adj.get(i).size() < symbolSet.size()) {
                System.err.println("Error: State " + i + "trasitions: " + dfa.adj.get(i).size() + " < " + symbolSet.size());
            }
        }

        return dfa;
    }

    public static DFA generateDoubleStateTest(int nStates) {
        DFA dfa = new DFA(nStates, Arrays.asList("a", "b"));
        dfa.initialState = 0;
        dfa.finalStates[nStates - 1] = true;
        for (int i = 0; i < nStates - 1; i++) {
            if (i % 2 == 0) {
                dfa.addTransition(i, i + 1, "a");
                dfa.addTransition(i + 1, i, "a");
                dfa.addTransition(i, i + 2, "b");
            } else {
                dfa.addTransition(i, i + 1, "b");
            }
        }
        if (nStates % 2 == 0) {
            dfa.addTransition(nStates - 2, nStates - 1, "b");
        }
        dfa.addTransition(nStates - 1, nStates - 1, "a");
        dfa.addTransition(nStates - 1, nStates - 1, "b");
        return dfa;
    }

    public static DFA generateBinMultN(int nStates) {
        DFA dfa = new DFA(nStates, Arrays.asList("0", "1"));
        dfa.initialState = 0;
        dfa.finalStates[0] = true;
        for (int i = 0; i < nStates; i++) {
            dfa.addTransition(i, (2 * i) % nStates, "0");
            dfa.addTransition(i, (2 * i + 1) % nStates, "1");
        }
        return dfa;
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
                        Integer to = adj.get(j).get(symbol);
                        if (to != null) {
                            min.adj.get(i).put(symbol, statePartition[to]);
                        }
                    }
                    break;
                }
            }
        }
        min.removeUselessStates();
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
                    Integer to1 = adj.get(partition.get(j).id).get(symbol);
                    Integer to2 = adj.get(newPartitions.get(i).get(0).id).get(symbol);
                    if (to1 == null || to2 == null) {
                        if (to1 != to2) {
                            belongsToPartition = false;
                        }
                    } else if (statePartition[to1] != statePartition[to2]) {
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

    // Minimizes the DFA using the Myphill-Nerode algorithm O(n^2)
    public DFA stdMinimization2(){
        int[][] table = new int[adj.size()][adj.size()];
        for (int i = 0; i < adj.size(); i++) {
            for (int j = 0; j < adj.size(); j++) {
                table[i][j] = -1;
            }
        }

        for (int i = 0; i < adj.size(); i++) {
            for (int j = 0; j < adj.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (finalStates[i] != finalStates[j]) {
                    table[i][j] = 0;
                }
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < adj.size(); i++) {
                for (int j = 0; j < adj.size(); j++) {
                    if (table[i][j] == -1) {
                        for (String symbol : symbolSet) {
                            int to1 = adj.get(i).get(symbol);
                            int to2 = adj.get(j).get(symbol);
                            if (table[to1][to2] == 0) {
                                table[i][j] = 0;
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Combine all the unmarked pair (Qi, Qj) and make them a single state in the reduced DFA.
        List<Integer> newStates = new ArrayList<>();
        int[] stateMap = new int[adj.size()];
        for (int i = 0; i < adj.size(); i++) {
            stateMap[i] = -1;
        }
        int n = 0;
        for (int i = 0; i < adj.size(); i++) {
            if (stateMap[i] == -1) {
                newStates.add(i);
                stateMap[i] = n;
                for (int j = i + 1; j < adj.size(); j++) {
                    if (table[i][j] == -1) {
                        newStates.add(j);
                        stateMap[j] = n;
                    }
                }
                n++;
            }
        }

        DFA min = new DFA(n, symbolSet);
        min.initialState = stateMap[initialState];
        for (int i = 0; i < adj.size(); i++) {
            for (String symbol : symbolSet) {
                int to = adj.get(i).get(symbol);
                min.addTransition(stateMap[i], stateMap[to], symbol);
            }
        }

        for (int i = 0; i < adj.size(); i++) {
            if (finalStates[i]) {
                min.finalStates[stateMap[i]] = true;
            }
        }

        return min;

    }

    private void removeUselessStates() {
        boolean[] visited = new boolean[adj.size()];
        List<Integer> reachableStates = new ArrayList<>();
        reachableStates.add(initialState);
        visited[initialState] = true;
        while (reachableStates.size() > 0) {
            int state = reachableStates.get(0);
            reachableStates.remove(0);
            for (String symbol : symbolSet) {
                Integer to = adj.get(state).get(symbol);
                if (to != null && !visited[to]) {
                    visited[to] = true;
                    reachableStates.add(to);
                }
            }
        }
        int useless = 0;
        for (int i = 0; i < adj.size(); i++) {
            if (!visited[i]) {
                // Reshift the states and update the transitions
                for (int j = 0; j < adj.size(); j++) {
                    for (String symbol : symbolSet) {
                        Integer to = adj.get(j).get(symbol);
                        if (to != null && to > i) {
                            adj.get(j).put(symbol, to - 1);
                        }
                    }
                }
                useless++;
            }
        }
        for (int i = 0; i < useless; i++) {
            adj.removeLast();
        }
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
        HashMap<Integer, Integer> stateMap = new HashMap<>();
        for (int i = 0; i < nStates; i++) {
            adj.add(new HashMap<>());
        }
        try {
            

            

            // Pega o caminho geral do programa
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
                                int id = Integer.parseInt(reader.getAttributeValue(null, "id"));
                                stateMap.put(id, nStates);
                                String name = reader.getAttributeValue(null, "name");
                                states.add(new State(nStates, name));
                                lastElement = "state";
                                nStates++;
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
                                currentTransition.to = stateMap.get(Integer.parseInt(text));
                                break;
                            case "from":
                                currentTransition.from =  stateMap.get(Integer.parseInt(text));
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
        String str = "\n";
        str += "Number of States: " + adj.size() + "\n";
        str += "Initial state: q" + initialState + "\n";
        str += "Final states: ";
        for (int i = 0; i < finalStates.length; i++) {
            if (finalStates[i]) {
                str += "q" + i + " ";
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
        DFA gen = DFA.generateDFA(10, Arrays.asList("a", "b"));
        System.out.println("Original Size: " + gen.adj.size());
        DFA min = gen.stdMinimization();
        System.out.println("Minimized Size: " + min.adj.size());
        String filename = "gen";
        gen.SaveJFLAPXML(filename + ".jff");
        min.SaveJFLAPXML(filename + "_min" + ".jff");
    }
}
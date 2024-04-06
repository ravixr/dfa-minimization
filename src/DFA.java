import java.util.List;

import javax.print.DocFlavor.STRING;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
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
    List<HashMap<String, Integer>> adj;
    int initialState;
    List<State> states;
    boolean[] finalStates;

    public DFA(int nStates, int initialState, int[] finalStates) {
        boolean[] finalStatesBool = new boolean[nStates];
        for (int i = 0; i < finalStates.length; i++) {
            finalStatesBool[finalStates[i]] = true;
        }
        this.initialState = initialState;
        this.finalStates = finalStatesBool;
        adj = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            adj.add(new HashMap<>());
        }
    }

    public DFA(int nStates, int initialState, boolean[] finalStates) {
        this.initialState = initialState;
        this.finalStates = finalStates;
        adj = new ArrayList<>();
        for (int i = 0; i < nStates; i++) {
            adj.add(new HashMap<>());
        }
    }

    public void addTransition(int from, int to, String symbol) {
        adj.get(from).put(symbol, to);
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
        for (int i = 0; i < nStates; i++) {
            adj.add(new HashMap<>());
        }
        try {
            

            

            // Pega o caminho geral do programa
            //System.out.println("Caminho do programa: " + System.getProperty("user.dir"));
            filePath = System.getProperty("user.dir") + "/src/" + filePath;
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

        new_dfa = new DFA(nStates, initialState, finalStates);
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
        DFA a = new DFA(3, 0, new boolean[]{true, false, false});
        a.addTransition(0, 0, "1");
        a.addTransition(0, 1, "0");
        a.addTransition(1, 2, "0");
        a.addTransition(2, 2, "0");

        System.out.println(a.toJFLAPXML());
    }
}
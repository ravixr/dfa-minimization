import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Main {
    public static void main(String[] args) throws Exception {
        
       // XMLParser parser = new XMLParser();
        DFA dfa = DFA.FromJFLAPXML("automato_test.jff");
        System.out.println(dfa.toString());
        dfa.SaveJFLAPXML("automato_test2.jff");
    }
}
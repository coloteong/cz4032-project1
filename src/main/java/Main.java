import java.io.*;
import java.util.*;
public class Main {
    public static void main(String[] args) throws Exception {


        Reader csvReader = new Reader();
        Transaction[] transactionList = csvReader.startReader();
        RG ruleGenerator = new RG(transactionList);
        ruleGenerator.getRuleItems();

        // Classifier classifier = new Classifier();
        // classifier.start();
        // classifier.findCRuleAndWRule();
        // classifier.goThroughDataAgain();
        // classifier.chooseFinalRules();
    }
}

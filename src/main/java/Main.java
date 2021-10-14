import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);
        System.out.println("CMAR or CBA? (0 or 1)");
        var choice = sc.nextInt();

        Reader csvReader = new Reader();
        Transaction[] transactionList = csvReader.startReader();
        if (choice == 1) {
            RG ruleGenerator = new RG(transactionList);
            var ruleArray = ruleGenerator.getRuleItems();
            for (Rule rule : ruleArray) {
                System.out.printf("Rule ID:%d, Rule Class:%d, Rule Antecedent:", rule.getRuleID(), rule.getConsequent());
                for (int item : rule.getAntecedent()) {
                    System.out.printf("%d, ", item);
                }
                System.out.println("");
            }
            Classifier classifier = new Classifier(ruleArray, transactionList);
            classifier.start();
            classifier.findCRuleAndWRule(); 
            classifier.goThroughDataAgain();
            var classifierRules = classifier.chooseFinalRules();
        } else if (choice == 0) {
            CMARRG CMARRuleGenerator = new CMARRG(transactionList);
            var ruleArray = CMARRuleGenerator.getRuleItems();
            for (Rule rule : ruleArray) {
                System.out.printf("Rule ID:%d, Rule Class:%d, Rule Antecedent:", rule.getRuleID(), rule.getConsequent());
                for (int item : rule.getAntecedent()) {
                    System.out.printf("%d, ", item);
                }
                System.out.println("");
            }
            CMARClassifier cmarClassifier = new CMARClassifier(ruleArray, transactionList);
            cmarClassifier.start();
            cmarClassifier.findClassifier();
        }
        sc.close();
    }
}

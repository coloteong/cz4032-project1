import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static Transaction[] trainTransactionList;
    public static Transaction[] testTransactionList;
    public static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);
        System.out.println("CMAR or CBA? (0 or 1)");
        var choice = sc.nextInt();

        Reader csvReader = new Reader();
        Transaction[] transactionList = csvReader.startReader();
        trainTestSplit(transactionList);
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
            CMARRG CMARRuleGenerator = new CMARRG(trainTransactionList);
            var ruleArray = CMARRuleGenerator.getRuleItems();
            for (Rule rule : ruleArray) {
                System.out.printf("Rule ID:%d, Rule Class:%d, Rule Antecedent:", rule.getRuleID(), rule.getConsequent());
                for (int item : rule.getAntecedent()) {
                    System.out.printf("%d, ", item);
                }
                System.out.println("");
            }

            CMARClassifier cmarClassifier = new CMARClassifier(ruleArray, testTransactionList);
            cmarClassifier.start();
            cmarClassifier.findClassifier();
        }
        sc.close();
    }

    private static void trainTestSplit(Transaction[] transactionList) {
        List<Transaction> transactionArrayList = Arrays.asList(transactionList);
        Collections.shuffle(transactionArrayList);
        int trainSize = (int) (0.7 * transactionArrayList.size());
        trainTransactionList = new Transaction[trainSize];
        testTransactionList = new Transaction[transactionArrayList.size() - trainSize];
        for (int i = 0; i < trainSize; i++) {
            trainTransactionList[i] = transactionArrayList.get(i);
        }
        for (int i = trainSize; i < transactionArrayList.size(); i++) {
            testTransactionList[i - trainSize] = transactionArrayList.get(i);
        }
    }
}

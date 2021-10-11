public class Main {
    public static void main(String[] args) throws Exception {


        Reader csvReader = new Reader();
        Transaction[] transactionList = csvReader.startReader();
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
    }
}

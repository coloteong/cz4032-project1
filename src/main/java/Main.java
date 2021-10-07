public class Main {
    public static void main(String[] args) throws Exception {
        RG rg = new RG();
        rg.start();
        // rg.createInitialItemsets();
        // rg.genRules();
        // rg.pruneRules();

        rg.generateFrequentItemsets();
        Classifier classifier = new Classifier();
        classifier.start();
        classifier.findCRuleAndWRule();
        classifier.goThroughDataAgain();
        classifier.chooseFinalRules();
    }
}

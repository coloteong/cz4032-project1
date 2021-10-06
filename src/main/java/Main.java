public class Main {
    public static void main(String[] args) throws Exception {
        RG rg = new RG();
        rg.start();
        rg.createInitialItemsets();
        rg.genRules();
        rg.pruneRules();

        for (int i = 2; rg.getItemsets().size() != 0; i++) {
            
        }

        rg.generateFrequentItemsets();
        Classifier classifier = new Classifier();
        classifier.start();
        classifier.findCRuleAndWRule();
        classifier.goThroughDataAgain();
        classifier.chooseFinalRules();
    }

}

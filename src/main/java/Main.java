public class Main {
    public static void main(String[] args) throws Exception {
        RG rg = new RG();
        rg.start();
        rg.generateFrequentItemsets();
        // rg.generateAssocRulesFromItemsets();
    }

}

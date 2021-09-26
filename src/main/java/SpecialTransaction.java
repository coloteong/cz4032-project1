public class SpecialTransaction {
    private int transactionID;
    private int transactionClass;
    private Rule cRule;
    private Rule wRule;

    public SpecialTransaction(int transactionID, int transactionClass, Rule cRule, Rule wRule) {
        this.transactionID = transactionID;
        this.transactionClass = transactionClass;
        this.cRule = cRule;
        this.wRule = wRule;
    }
}

public class SpecialRule {
    private Rule cRule;
    private int transactionID;
    private int transactionClass;

    public SpecialRule(int transactionID, int transactionClass, Rule cRule) {
        this.transactionID = transactionID;
        this.transactionClass = transactionClass;
        this.cRule = cRule;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public int getTransactionClass() {
        return transactionClass;
    }

    public Rule getCRule() {
        return cRule;
    }
}

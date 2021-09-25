public class Transaction {
    private static int count = 0;
    // the unique id for this transaction
    private int transactionID;
    // the classification for this transaction
    private int transactionClass;
    private int[] transactionItems;
    // these two fields will be null unless
    // we need to explicitly fill it due to 
    // problems in classification
    private Rule cRule = null;
    private Rule wRule = null;

    public Transaction(int transactionClass, int[] transactionItems) {
        this.transactionClass = transactionClass;
        this.transactionItems = transactionItems;
        
        transactionID = count++;
    }

    public void setCRule(Rule cRule) {
        this.cRule = cRule;
    }

    public void setWRule(Rule wRule) {
        this.wRule = wRule;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public void setTransactionClass(int transactionClass) {
        this.transactionClass = transactionClass;
    }

    public void setTransactionItems(int[] transactionItems) {
        this.transactionItems = transactionItems;
    }

    public Rule getCRule() {
        return cRule;
    }

    public Rule getWRule() {
        return wRule;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public int getTransactionClass() {
        return transactionClass;
    }

    public int[] getTransactionItems() {
        return transactionItems;
    }

}

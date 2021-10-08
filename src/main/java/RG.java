import org.apache.commons.lang3.*;
import java.io.*;
import java.util.*;
public class RG {


    // the number of itemsets
    private int numItems;
    // the number of columns in the data
    private int numColumns;
    // the number of transactions in the source file
    private static int numTransactions;
    public Transaction[] transactionList;
    private double minSup = 0.01;
    private double minConf = 0.1;
    // stores all the rules
    private ArrayList<Rule> ruleArray = new ArrayList<>();

    public RG(Transaction[] transactionList) {
        this.transactionList = transactionList;
    }

    public void getRuleItems() {
        ruleArray = createInitialRuleItems();
        ruleArray = genRules();
        // seems like our create initial rule items is already genRules

    }

    private ArrayList<Rule> createInitialRuleItems() {
        // generate all the rule items with one item on the antecedent
        // which is also frequent
        ArrayList<Rule> currRuleArray = new ArrayList<>();
        Set<Integer> itemSet = new HashSet<>();
        Set<Integer> possibleClasses = new HashSet<>();
        for (Transaction transaction : transactionList) {
            possibleClasses.add(transaction.getTransactionClass());
            for (int item : transaction.getTransactionItems()) {
                itemSet.add(item);
            }
        }

        for (Integer item : itemSet) {
            var i = 0;
            int[] ruleItem = {item};
            Rule initialRule = new Rule(ruleItem);

            for (Integer ruleClass : possibleClasses) {
                if (i == 0) {
                    initialRule.setConsequent(ruleClass);
                } else {
                    Rule rule = new Rule(ruleItem, ruleClass);
                    if (rule.getConfidence() > initialRule.getConfidence()) {
                        initialRule = rule;
                    }
                }
                i++;
            }

            if (initialRule.getSupport() > minSup) {
                currRuleArray.add(initialRule);
            }
        }
        System.out.println(currRuleArray.size());
        return currRuleArray;
    }


    //TODO #7
    public void generateFrequentItemsets() {
        createInitialItemsets();
        generateAssocRulesFromItemsets();
        // pruneRules();
        int itemsetNumber = 1;
        while (!itemsets.isEmpty()) {
            System.out.println("Itemsets.size: " + itemsets.size());
            itemsets = calculateFrequentItemsets();
            System.out.println("Frequent Itemset size: " + itemsets.size());
            if (!itemsets.isEmpty()) {
                System.out.println("found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber);
                itemsets =  createNewItemsetsFromPrevious();
            }
            System.out.println("Created the new itemsets; generating asssoc rules now");
            generateAssocRulesFromItemsets();
            System.out.println("Size of ruleArray: " + ruleArray.size());
            // TODO: #13 pruneRules takes too long
            pruneRules();
            System.out.println("Size of ruleArray after pruning: " + ruleArray.size());
            itemsetNumber++;
        }
    }

    public void generateAssocRulesFromItemsets() {
        // for each itemset, we get a rule
        // let's use variant 2 of mining association rules from the lecture notes
        for (Itemset itemset : itemsets) {
            int rightHandSide = itemset.getItems()[0];
            // everything else is in the antecedent
            var leftHandSide = Arrays.copyOfRange(itemset.getItems(), 1, itemset.getItems().length);
            Rule newRule = new Rule(leftHandSide, rightHandSide);
            // for this rule, we check if it is above the min confidence
            var minConf = 0.4;
            if (newRule.getConfidence() > minConf) {
                ruleArray.add(newRule);
            }
        }
    }


    private ArrayList<Rule> ruleSubset(List<Rule> ruleSet, Transaction transaction) {
        ArrayList<Rule> ruleArrayList = new ArrayList<>();
        for (Rule rule : ruleArrayList) {
            var ruleCondSet = rule.getAntecedent();
            var supported = true;
            for (int ruleItem : ruleCondSet) {
                if (!ArrayUtils.contains(transaction.getTransactionItems(), ruleItem)) {
                    supported = false;
                    break;
                }
            }
            if (supported) {
                ruleArrayList.add(rule);
            }
        }
        return ruleArrayList;
    }

    private void candidateGen() {

    }
    
    private ArrayList<Rule> genRules() {
        for (Rule rule : ruleArray) {
            if (rule.getConfidence() < minConf) {
                ruleArray.remove(rule);
            }
        }
        return ruleArray;
    }

    // FIXME #16
    private ArrayList<Itemset> createNewItemsetsFromPrevious() {
        // get the number of items in the current candidate itemset
        int currentItemsetSize = itemsets.get(0).getItems().length;
        System.out.println("generating frequent candidate frequent itemsets of size " + (currentItemsetSize + 1) );
        // HashMap<String, int[]> freqCandidates = new HashMap<>();
        ArrayList<Itemset> freqCandidates = new ArrayList<>();
        for (int i = 0; i < itemsets.size(); i++) {
            for (int j = 0; j < itemsets.size(); j++) {
                if (j != i) {
                    var X = itemsets.get(i).getItems();
                    // using array X as the base, we make the first n - 1 elements of the next itemset
                    // the elements of X
                    int[] newCand = new int[X.length + 1];
                    for (int k = 0; k < X.length; k++) {
                        newCand[k] = X[k];
                    }
                    // we would then want to check for elements in the frequent n - 1 itemsets that are also frequent
                    // but which has an element not in x

                    int nDifferent = 0;
                    boolean found = false;
                    for (int item : itemsets.get(j).getItems()) {
                        for (int x : X) {
                            if (x == item) {
                                break;
                            } 
                            if (!found) {
                                nDifferent++;
                                var difference = item;
                            }
                        }
                        // if there is such an element
                        // we add this to the last position of the new candidate itemset
                        if (nDifferent == 1) {
                            newCand[newCand.length - 1] = item;
                            Itemset newCandidateItemset = new Itemset(newCand);
                            freqCandidates.add(newCandidateItemset);
                        }
                    }
                    // add this new frequent itemeset of length n
                    // and put it into the freqCandidates list
            }
        }
    }
    return freqCandidates;
    }


    public void createInitialItemsets() {
        // itemsets will be an int of all the items
        itemsets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i + 1};
            Itemset itemset = new Itemset(cand);
            itemsets.add(itemset);
        }
    }

    private void convertToTransactionList(int classColumn) {

        transactionList = new Transaction[numTransactions];
        for (int i = 0; i < numTransactions; i++) {
            int transactionClass = 0;
            int[] transactionItems = new int[numColumns - 1];
            for (int j = 0; j < numColumns; j++) {
                if (j == classColumn) {
                    transactionClass = dataArray.get((i * numColumns) + j);
                } else {
                    transactionItems[j - 1] = dataArray.get((i * numColumns) + j);
                }
            }
            Transaction transaction = new Transaction(transactionClass, transactionItems);
            transactionList[i] = transaction;
        }
    }

    private List<Itemset> calculateFrequentItemsets() {
        var size = itemsets.get(0).getItems().length;

        System.out.println("Calculating frequent itemsets to compute the frequency of itemsets of size " + size);
        List<Itemset> frequentCandidates = new ArrayList<>();
        for (Itemset itemset : itemsets) {  
            if (itemset.getSupport() >= minSup) 
                frequentCandidates.add(itemset);
        }
        return frequentCandidates;
    }


    public static double countSupport(int[] items) {
        /*
          match: whether the transaction has al the items in an itemset
          count: number of successful matches
         */
        boolean match;
        int count = 0;

        // check items against each transaction
        for (int i = 0; i < numTransactions; i++) {
            match = true;
            // set match to false if there is an item from items is missing in transaction
            for (int c: items) {
                if (!ArrayUtils.contains(transactionList[i].getTransactionItems(), c)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                count++;
            }
        }
        return count / (double) (numTransactions);
    }

    public void pruneRules() {
        // prune the rules in the current Rule Array 
        var lastRuleSize = ruleArray.get(ruleArray.size() - 1).getAntecedent().length;
        var start = 0;
        
        for (int k = start; k < ruleArray.size(); k++) {
        // for (Rule rule : currRuleArray) {
            var rule = ruleArray.get(k);
            System.out.println(rule.getRuleID());
            var itemsInLHS = rule.getAntecedent().length;
            // we can only have an R^minus if the number of items on the LHS
            // is more than 1
            if (itemsInLHS > 1) {
                // for the number of items in the LHS = n
                // we can get n rules of size n - 1
                // using N choose N - 1
                for (int i = 0; i < itemsInLHS; i++) {
                    int[] newAntecedent = new int[itemsInLHS - 1];
                    // what we do is we choose which item to ignore i.e. i
                    for (int j = 0; j < newAntecedent.length; j++) {
                        if (j != i)
                            newAntecedent[j] = rule.getAntecedent()[i];
                        else
                            j--;
                    }
                    Rule rMinus = new Rule(newAntecedent, rule.getConsequent());
                    // if (!ruleError.containsKey(rMinus)) {
                    //     ruleError.put(rMinus, (float) countPessimisticError(rMinus));
                    // }
                    // var rMinusError = ruleError.get(rMinus);
                    if (countPessimisticError(rule) > countPessimisticError(rMinus)) {
                        ruleArray.remove(rule);
                    }
                }
            }
            else {
                break;
            }
        }
    }

    private double countPessimisticError(Rule rule) {
        // since not all transactions will apply to a rule
        // we have to calculate the error based on the number 
        // of transactions that can be applied as well as 
        // the number of wrong transactions
        var numWrongTransactions = 0;
        var numApplicableTransactions = 0;
        for (Transaction transaction : transactionList) {
            var transactionClass = transaction.getTransactionClass();
            var transactionItems = transaction.getTransactionItems();
            var ruleLHS = rule.getAntecedent();
            var ruleClass = rule.getConsequent();
            var match = true;

            for (int item : transactionItems) {
                if (!ArrayUtils.contains(ruleLHS, item)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                numApplicableTransactions++;
                if (ruleClass != transactionClass)
                    numWrongTransactions++;
            }
        }
        double trainingError = numWrongTransactions / numApplicableTransactions;
        double pessimisticError = (numWrongTransactions + (rule.getAntecedent().length * 2)) / (double) numApplicableTransactions;
        return pessimisticError;
    }


}

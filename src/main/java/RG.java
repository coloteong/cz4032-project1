import org.apache.commons.lang3.*;

import java.io.*;
import java.util.*;
public class RG {

    // the number of transactions in the source file
    public static int numTransactions;
    public Transaction[] transactionList;
    private double minSup = 0.01;
    private double minConf = 0.3;
    // stores all the rules
    private ArrayList<Rule> ruleArray = new ArrayList<>();

    public RG(Transaction[] transactionList) {
        this.transactionList = transactionList;
        numTransactions = transactionList.length;
    }

    public void getRuleItems() {
        List<Rule> candidateRules;
        List<Rule> currRuleArray;
        // line 1
        currRuleArray = createInitialRuleItems();
        System.out.printf("Size of ruleArray at 1: ");
        System.out.println(currRuleArray.size());
        // line 2
        currRuleArray = genRules(currRuleArray);
        System.out.printf("Size of ruleArray at 2: ");
        System.out.println(currRuleArray.size());
        ruleArray.addAll(currRuleArray);
        // skip pruning for large 1 itemset
        while (!currRuleArray.isEmpty()) {
            candidateRules = candidateGen(currRuleArray);
            System.out.printf("Size of candidateRules: %d, number of items in antecedent: %d", candidateRules.size(), candidateRules.get(0).getAntecedent().length);
            currRuleArray = new ArrayList<>();
            for (Transaction transaction : transactionList) {
                var cD = ruleSubset(candidateRules, transaction);
                for (Rule rule : cD) {
                    rule.incrementCondSupCount();
                    if (transaction.getTransactionClass() == rule.getConsequent()) {
                        rule.incrementRuleSupCount();
                    }
                }
            }

            for (Rule rule : candidateRules) {
                if (rule.getRuleSupportCount() > minSup) {
                    currRuleArray.add(rule);
                }
            }

            currRuleArray = genRules(currRuleArray);
            for (Rule rule : currRuleArray) {
                System.out.println("List of rules: ");
                System.out.println(rule.getRuleID());
            }

            ruleArray.addAll(currRuleArray);
            }
    }

    private ArrayList<Rule> createInitialRuleItems() {
        // generate all the rule items with one item on the antecedent
        // which is also frequent
        // ArrayList<Rule> currRuleArray = new ArrayList<>();
        ArrayList<Rule> possibleRules = new ArrayList<>();
        Set<Integer> itemSet = new HashSet<>();
        Set<Integer> possibleClasses = new HashSet<>();

        for (Transaction transaction : transactionList) {
            possibleClasses.add(transaction.getTransactionClass());
            for (int item : transaction.getTransactionItems()) {
                System.out.println(item);
                itemSet.add(item);
            }
        }

        for (Integer item : itemSet) {
            Map<Integer, Integer> classMap = new HashMap<>();
            int[] ruleAntecedent = {item};
            int numCond = 0;
            for (Integer possibleClass : possibleClasses) {
                numCond = 0;
                var numClass = 0;
                for (Transaction transaction : transactionList) {
                    if (ArrayUtils.contains(transaction.getTransactionItems(), item)) {
                        numCond++;
                        if (possibleClass == transaction.getTransactionClass()) {
                            numClass++;
                        }
                    }
                }
                classMap.put(numClass, possibleClass);
            }
            int max = Collections.max(classMap.keySet());
            Rule rule = new Rule(ruleAntecedent, classMap.get(max), numCond, max);

            System.out.printf("Antecedent:");
            System.out.println(rule.getAntecedent()[0]);
            System.out.printf("Consequent:");
            System.out.println(rule.getConsequent());
            System.out.printf("Support:");
            System.out.println(rule.getSupport());

            if (rule.getSupport() > minSup) {
                possibleRules.add(rule);
            }
        }
        return possibleRules;
    }


    // //TODO #7
    // public void generateFrequentItemsets() {
    //     createInitialItemsets();
    //     generateAssocRulesFromItemsets();
    //     // pruneRules();
    //     int itemsetNumber = 1;
    //     while (!itemsets.isEmpty()) {
    //         System.out.println("Itemsets.size: " + itemsets.size());
    //         itemsets = calculateFrequentItemsets();
    //         System.out.println("Frequent Itemset size: " + itemsets.size());
    //         if (!itemsets.isEmpty()) {
    //             System.out.println("found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber);
    //             itemsets =  createNewItemsetsFromPrevious();
    //         }
    //         System.out.println("Created the new itemsets; generating asssoc rules now");
    //         generateAssocRulesFromItemsets();
    //         System.out.println("Size of ruleArray: " + ruleArray.size());
    //         // TODO: #13 pruneRules takes too long
    //         pruneRules();
    //         System.out.println("Size of ruleArray after pruning: " + ruleArray.size());
    //         itemsetNumber++;
    //     }
    // }

    // public void generateAssocRulesFromItemsets() {
    //     // for each itemset, we get a rule
    //     // let's use variant 2 of mining association rules from the lecture notes
    //     for (Itemset itemset : itemsets) {
    //         int rightHandSide = itemset.getItems()[0];
    //         // everything else is in the antecedent
    //         var leftHandSide = Arrays.copyOfRange(itemset.getItems(), 1, itemset.getItems().length);
    //         Rule newRule = new Rule(leftHandSide, rightHandSide);
    //         // for this rule, we check if it is above the min confidence
    //         var minConf = 0.4;
    //         if (newRule.getConfidence() > minConf) {
    //             ruleArray.add(newRule);
    //         }
    //     }
    // }


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
        for (Rule rule : ruleArrayList) {
            System.out.println(rule.getRuleID());
        }
        
        return ruleArrayList;
    }

    private List<Rule> candidateGen(List<Rule> ruleArray) {
        List<Rule> candidateRules = new ArrayList<>();
        Set<int[]> generatedRuleAntecedents = new HashSet<>();

        for (int i = 0; i < ruleArray.size(); i++) {
                
            Rule rule = ruleArray.get(i);
            var ruleAntecedents = rule.getAntecedent();
            int[] newRuleAntecedents = new int[ruleAntecedents.length + 1];

            for (int j = 0; j < ruleAntecedents.length; j++) {
                newRuleAntecedents[j] = ruleAntecedents[j];
            }

            System.out.printf("Rule 1 antecedent: %d\n", rule.getAntecedent()[0]);
            for (int j = 0; j < i; j++) {
                
                Rule rule2 = ruleArray.get(j);
                var contains = true;
                var nDiff = 0;
                var rule2Antecedents = rule2.getAntecedent();

                if (rule.getConsequent() == rule2.getConsequent()) {
                    for (int k = 0; k < rule2Antecedents.length; k++) {
                        System.out.printf("rule 2 antecedents : %d \n", rule2Antecedents[k]);
                        if (!ArrayUtils.contains(newRuleAntecedents, rule2Antecedents[k])) {
                            contains = false;
                            nDiff++;
                        }
                    }

                    System.out.printf("nDiff = %d\n", nDiff);

                    if (!contains) {
                        if (nDiff == 1) {
                            for (int k = 0; k < rule2Antecedents.length; k++) {
                                if (!ArrayUtils.contains(newRuleAntecedents, rule2Antecedents[k])) {
                                    System.out.println("Here we are");
                                    newRuleAntecedents[newRuleAntecedents.length - 1] = rule2Antecedents[k];
                                }
                            }
                        }
                    }

                    for (int k : newRuleAntecedents) {
                        System.out.printf("Antecedent: %d \n", k);
                    }
                    
                    if (!ArrayUtils.contains(newRuleAntecedents, 0)) {
                            Rule biggerRule = new Rule(newRuleAntecedents, rule2.getConsequent());
                            System.out.println("Antecedents of the New Rule");
                            for (int item : newRuleAntecedents) {
                                System.out.printf("%d, ", item);
                            }
                            System.out.printf("\n");
                            generatedRuleAntecedents.add(newRuleAntecedents);
                            candidateRules.add(biggerRule);
                    }
                }
            }
        }

        return candidateRules;
    }
    
    private ArrayList<Rule> genRules(List<Rule> ruleArray) {
        List<Rule> currRuleArray = new ArrayList<>();
        for (Rule rule : ruleArray) {
            System.out.printf("Antecedent: %d, Consequent: %d, Confidence: %f \n", rule.getAntecedent()[0], rule.getConsequent(), rule.getConfidence());
            if (rule.getConfidence() > minConf) {
                currRuleArray.add(rule);
            }
        }
        return (ArrayList<Rule>) currRuleArray;
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

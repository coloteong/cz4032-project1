import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.primitives.Ints;

import org.apache.commons.lang3.ArrayUtils;

public class CMARRG {
    
    // the number of transactions in the source file
    public static int numTransactions;
    public Transaction[] transactionList;
    private double minSup = 0.01;
    private double minConf = 0.5;
    private int minThreshold = 3;
    // stores all the rules
    private ArrayList<Rule> ruleArray = new ArrayList<>();

    private Map<Integer, Integer> classDistr = new HashMap<>();

    public CMARRG(Transaction[] transactionList) {
        this.transactionList = transactionList;
        numTransactions = transactionList.length;
        classDistr = compClassDistri(transactionList);
    }

    public List<Rule> getRuleItems() {
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
            System.out.printf("Candidate Rules size: %d\n", candidateRules.size());
            for (Rule rule : candidateRules) {
                System.out.printf("Rule ID: %d, Rule Consequent: %d, Rule Antecedent: ", rule.getRuleID(), rule.getConsequent());
                for (int antecedentItem : rule.getAntecedent()) {
                    System.out.printf("%d", antecedentItem);
                }
                System.out.println("");
            }
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
                System.out.printf("Rule class: %d, Rule Support: %f, Rule antecedent:", rule.getConsequent(), rule.getSupport());
                for (int antecedentItem : rule.getAntecedent()) {
                    System.out.printf("%d, ", antecedentItem);
                }
                System.out.println("");
                if (rule.getSupport() >= minSup) {
                    System.out.println("We are here");
                }
            
            }

            currRuleArray = genRules(currRuleArray);
            // var prunedRuleArray = pruneCMARRules(currRuleArray);

            ruleArray.addAll(currRuleArray);
            }
        ruleArray = finalCMARPruning(ruleArray);
        return ruleArray;
    }
    private float calculateChiSquared(Rule rule) {
        int grandTotal = transactionList.length;
        // these are the observed values
        int pAndC = rule.getRuleSupportCount();
        int pAndNotC = (int) ((1 - rule.getConfidence()) * rule.getCondSupportCount());
        int allWithC = classDistr.get(rule.getConsequent());
        int notPAndC = allWithC - pAndC;
        int notPAndNotC = grandTotal - pAndC - pAndNotC - notPAndC;

        // calculate the marginal total
        int rowOneMarginalTotal = pAndC + pAndNotC;
        int rowTwoMarginalTotal = notPAndC + notPAndNotC;
        int colOneMarginalTotal = pAndC + notPAndC;
        int colTwoMarginalTotal = pAndNotC + notPAndNotC;

        // calculate the expected values
        float expectedPAndC =  ((float) rowOneMarginalTotal * colOneMarginalTotal) / grandTotal;
        float expectedPAndNotC = ((float) rowOneMarginalTotal * colTwoMarginalTotal) / grandTotal;
        float expectedNotPAndC = ((float) rowTwoMarginalTotal * colOneMarginalTotal) / grandTotal;
        float expectedNotPAndNotC = ((float) rowTwoMarginalTotal * colTwoMarginalTotal) / grandTotal;

        // calculate Chi Squared
        var firstElement = Math.pow((pAndC - expectedPAndC), 2) / expectedPAndC;
        var secondElement = Math.pow((notPAndC - expectedNotPAndC), 2) / expectedNotPAndC;
        var thirdElement = Math.pow((pAndNotC - expectedPAndNotC), 2) / expectedPAndNotC;
        var fourthElement = Math.pow((notPAndNotC - expectedNotPAndNotC), 2) / expectedNotPAndNotC;

        return (float) (firstElement + secondElement + thirdElement + fourthElement);
    }

    private HashMap<Integer, Integer> compClassDistri(Transaction[] transactionList) {
        HashMap<Integer, Integer> classDistr = new HashMap<>();
        for (Transaction transaction : transactionList) {
            var transactionClass = transaction.getTransactionClass();
            if (classDistr.get(transactionClass) == null) {
                classDistr.put(transactionClass, 1);
            } else {
                classDistr.put(transactionClass, classDistr.get(transactionClass) + 1);
            }
        }
        return classDistr;
    }


    private ArrayList<Rule> finalCMARPruning(ArrayList<Rule> ruleArray) {
        Collections.sort(ruleArray);
        List<Transaction> tempTransactions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            tempTransactions.add(transaction);
        }
        // ArrayList<Integer> coverCounts = new ArrayList<>();
        int[] coverCounts = new int[transactionList.length];
        ArrayList<Rule> rulesCorrectlyClassify = new ArrayList<>();

        // if they are the same size, there is nothing to prune

            for (Rule rule : ruleArray) {
                for (Transaction transaction : tempTransactions) {
                    // if (rule.getAntecedent() == transaction.getTransactionItems()) {
                    boolean match = true;
                    for (int ruleItem : rule.getAntecedent()) {
                        if (!ArrayUtils.contains(transaction.getTransactionItems(), ruleItem)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        if (rule.getConsequent() == transaction.getTransactionClass()) {
                            if (!rulesCorrectlyClassify.contains(rule)) {
                                rulesCorrectlyClassify.add(rule);
                                coverCounts[transaction.getTransactionID()]++;
                            }
                        }
                    }
                }
            }
            // data object if covercount is higher than threshold
            for (int i = 0; i < coverCounts.length; i++) {
                if (coverCounts[i] >= minThreshold) {
                    tempTransactions.remove(i);
                }
            }

        return rulesCorrectlyClassify;
    }

        // for (Rule rule : rulesCorrectlyClassify) {
        //     for (Transaction transaction : tempTransactions) {
        //         if (rule.getAntecedent() == transaction.getTransactionItems()) {
        //             if (rule.getConsequent() == transaction.getTransactionClass()) {
        //                 // think this might not work 
        //                 Integer indexOfTransaction = java.util.Arrays.asList(tempTransactions).indexOf(transaction);
        //                 coverCounts.set(indexOfTransaction, coverCounts.get(indexOfTransaction) + 1); 
        //             }
        //         }
        //     }
        // }

    private ArrayList<Rule> createInitialRuleItems() {
        // generate all the rule items with one item on the antecedent
        // which is also frequent
        ArrayList<Rule> possibleRules = new ArrayList<>();
        Set<Integer> itemSet = new HashSet<>();
        Set<Integer> possibleClasses = new HashSet<>();

        for (Transaction transaction : transactionList) {
            possibleClasses.add(transaction.getTransactionClass());
            for (int item : transaction.getTransactionItems()) {
                System.out.printf("Stupid Error:%d\n", item);
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
            System.out.printf("numCond: %d, max: %d\n", numCond, max);
            Rule rule = new Rule(ruleAntecedent, classMap.get(max), numCond, max);

            System.out.printf("Antecedent:");
            System.out.println(rule.getAntecedent()[0]);
            System.out.printf("Consequent:");
            System.out.println(rule.getConsequent());
            System.out.printf("Support:");
            System.out.println(rule.getSupport());

            if (rule.getSupport() >= minSup) {
                possibleRules.add(rule);
            }
        }
        return possibleRules;
    }

    private ArrayList<Rule> ruleSubset(List<Rule> ruleSet, Transaction transaction) {
        ArrayList<Rule> ruleArrayList = new ArrayList<>();
        for (Rule rule : ruleSet) {
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

        System.out.printf("Rule Array List Size: %d \n", ruleArrayList.size());
        return ruleArrayList;
    }

    private List<Rule> candidateGen(List<Rule> ruleArray) {
        var count = 0;
        List<Rule> candidateRules = new ArrayList<>();
        Set<List<Integer>> generatedRuleAntecedents = new HashSet<>();

        for (int i = 0; i < ruleArray.size(); i++) {
            if (candidateRules.size() + ruleArray.size() > 10000) {
                break;
            }
                
            Rule rule = ruleArray.get(i);
            var ruleAntecedents = rule.getAntecedent();


            System.out.printf("Rule 1 antecedent: %d\n", rule.getAntecedent()[0]);
            for (int j = 0; j < i; j++) {
                
                int[] newRuleAntecedents = new int[ruleAntecedents.length + 1];

                for (int k = 0; k < ruleAntecedents.length; k++) {
                    newRuleAntecedents[k] = ruleAntecedents[k];
                }

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
                                if (!ArrayUtils.contains(newRuleAntecedents, 0)) {
                                        Arrays.sort(newRuleAntecedents);
                                        List<Integer> generatedAntecedent = Ints.asList(newRuleAntecedents);
                                        if(!generatedRuleAntecedents.contains(generatedAntecedent)) {
                                            Rule biggerRule = new Rule(newRuleAntecedents, rule2.getConsequent());
                                            System.out.printf("Antecedants of biggerRule: ");
                                            for (int item1 : biggerRule.getAntecedent()) {
                                                System.out.printf("%d, ", item1);
                                            }
                                            System.out.printf("\n");
                                            System.out.printf("Class of biggerRule: %d", biggerRule.getConsequent());
                                            System.out.println("Antecedents of the New Rule");
                                            for (int item : newRuleAntecedents) {
                                                System.out.printf("%d, ", item);
                                            }
                                            System.out.printf("\n");
                                            generatedRuleAntecedents.add(generatedAntecedent);
                                            candidateRules.add(biggerRule);
                                            System.out.printf("Generated rule number: %d", count++);
                                            System.out.printf("\n");
                                            count++;
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
               
        }
        return candidateRules;
    }

    private ArrayList<Rule> genRules(List<Rule> ruleArray) {
        var bigRuleArray = this.ruleArray;
        List<Rule> currRuleArray = new ArrayList<>();
        for (Rule rule : ruleArray) {
            System.out.printf("Antecedent: %d, Consequent: %d, Confidence: %f \n", rule.getAntecedent()[0], rule.getConsequent(), rule.getConfidence());
            if (rule.getConfidence() > minConf) {
                boolean willAdd = true;
                var ruleLHS = rule.getAntecedent();
                // first CMAR Pruning is done here
                Collections.sort(bigRuleArray);
                for (Rule rule2 : bigRuleArray) {
                    var rule2LHS = rule2.getAntecedent();
                    boolean match = true;
                    for (int ruleItem : ruleLHS) {
                        if (!ArrayUtils.contains(rule2LHS, ruleItem)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        if (rule2.compareTo(rule) == -1) {
                            willAdd = false;
                            break;
                        }
                    }
                }
                if (willAdd) {
                    if (calculateChiSquared(rule) > 0 ){
                        currRuleArray.add(rule);
                    }
                }
            }
        }
        return (ArrayList<Rule>) currRuleArray;
    }
}

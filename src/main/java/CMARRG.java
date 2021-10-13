import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.netlib.util.booleanW;
import org.netlib.util.intW;

public class CMARRG {
    
    // the number of transactions in the source file
    public static int numTransactions;
    public Transaction[] transactionList;
    private double minSup = 0.01;
    private double minConf = 0.5;
    // stores all the rules
    private ArrayList<Rule> ruleArray = new ArrayList<>();

    public CMARRG(Transaction[] transactionList) {
        this.transactionList = transactionList;
        numTransactions = transactionList.length;
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
                    currRuleArray.add(rule);
                }
            }

            currRuleArray = genRules(currRuleArray);
            var prunedRuleArray = pruneCMARRules(currRuleArray);

            ruleArray.addAll(prunedRuleArray);
            }
        ruleArray = finalCMARPruning(ruleArray);
        return ruleArray;
    }

    private ArrayList<Rule> finalCMARPruning(ArrayList<Rule> ruleArray) {
        return null;
    }

    private List<Rule> pruneCMARRules(List<Rule> ruleArray) {
        return null;
    }

    private ArrayList<Rule> createInitialRuleItems() {
        // generate all the rule items with one item on the antecedent
        // which is also frequent
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
                    currRuleArray.add(rule);
                }
            }
        }
        return (ArrayList<Rule>) currRuleArray;
    }
}

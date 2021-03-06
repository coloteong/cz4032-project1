import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.*;

public class Classifier{
    private List<Rule> sortedRuleArray = new ArrayList<>();
    private Transaction[] transactionList;
    // this is set U in the paper
    private List<Rule> setOfCRules = new ArrayList<>();
    // this is set Q in the paper
    private List<Rule> setOfCRulesWithHigherPrecedence = new ArrayList<>();
    // this is set A in the paper
    private List<SpecialTransaction> setOfSpecialTransactions = new ArrayList<>();
    private ArrayList<Transaction> coveredTransaction = new ArrayList<>();
    // this is set C in the paper
    private ArrayList<ClassificationRule> classifierRules = new ArrayList<>();
    private ArrayList<Rule> finalClassifier = new ArrayList<>();
    public static int finalDefaultClass;

    public Classifier(List<Rule> ruleArray, Transaction[] transactionList) {
        sortedRuleArray = ruleArray;
        this.transactionList = transactionList;
    } 

    public void start() {
        Collections.sort(sortedRuleArray);
        /* for (Rule rule : sortedRuleArray) {
            System.out.printf("Rule ID:%d, Rule Confidence:%f, Rule Support:%f\n", rule.getRuleID(), rule.getConfidence(), rule.getSupport());
        } */
    }
    // CBA-CB M2 Stage 1
    public void findCRuleAndWRule() {

        for (Transaction transaction : transactionList) {
            //System.out.printf("Transaction ID: %d, Transaction Class: %d\n", transaction.getTransactionID(), transaction.getTransactionClass());
            var transactionItems = transaction.getTransactionItems();
            for (Rule rule : sortedRuleArray) {
                var ruleLHS = rule.getAntecedent();
                var match = true;
                for (int ruleItem : ruleLHS) {
                    if (!ArrayUtils.contains(transactionItems, ruleItem)) {
                        match = false;
                    }
                }
                if (match) {
                    if (rule.getConsequent() == transaction.getTransactionClass()) {
                        if(transaction.getCRule() == null) {
                            transaction.setCRule(rule);
                            setOfCRules.add(rule);
                            rule.addClassCasesCovered(transaction.getTransactionClass());
                            //System.out.printf("Transaction CRule ID: %d, Transaction CRule Class: %d\n", transaction.getCRule().getRuleID(), rule.getConsequent());
                        }
                    } else {
                        if(transaction.getWRule() == null) {
                            transaction.setWRule(rule);
                            //System.out.printf("Transaction wRule ID: %d, Transaction WRule Class: %d\n", transaction.getWRule().getRuleID(), rule.getConsequent());
                        }
                    }
                }
            }

            /*
            * so we can have four different situations depending on whether CRule and WRule are null or not null
            * 1. both are not null -> standard condition, things are as per normal
            * 2. CRule is null -> this occurs when all the rules that are generated that are applicable to the rule
            * gives the wrong class; hence, there is no rule that classifies this transaction correctly
            * 3. WRule is null -> this occurs when all the rules that are generated that are applicable to this rule
            * gives the correct class; hence there is no rule that classifies this transaction wrongly
            * 4. both are null -> none of the rules contain any element in this transaction ->
            * this can happen because maybe this is the only transaction with weird integers
            * this should not happen since we are discretizing 
            * 
            */

                // if cRule has a higher precedence
                // if wRule is null, we do not need to care since that means all rules will correctly classify
                // else we check the precedence

                if (((sortedRuleArray.indexOf(transaction.getCRule()) < sortedRuleArray.indexOf(transaction.getWRule())) || transaction.getWRule() == null) && transaction.getCRule() != null) {
                // case 1 comes here
                // case 3 comes here
                // need to mark CRule
                    //System.out.printf("Transaction %d is here\n", transaction.getTransactionID());
                    if(!setOfCRulesWithHigherPrecedence.contains(transaction.getCRule())) {
                        setOfCRulesWithHigherPrecedence.add(transaction.getCRule());
                        transaction.getCRule().markRule();
                    }
                }            
            if ((sortedRuleArray.indexOf(transaction.getWRule()) < sortedRuleArray.indexOf(transaction.getCRule()) || transaction.getCRule() == null) && transaction.getWRule() != null){
                // case 1 can also come here 
                // case 2 comes here
                // if cRule > wRule, need to keep a data structure containing:
                // transactionID, transactionClass, cRule, and wRule
                //System.out.printf("Transaction %d cannot be decided\n", transaction.getTransactionID());
                SpecialTransaction specialTransaction = new SpecialTransaction(transaction.getTransactionID(), transaction.getTransactionClass(), transaction.getCRule(), transaction.getWRule());
                setOfSpecialTransactions.add(specialTransaction);
                // we need to check setOfSpecialTransactions for cases in which CRule is null
            }

            // if it is case 4, we do not do anything...
            // need to check how to deal with this gracefully

        }
    }

    // CBA-CB: M2 (Stage 2)
    public void goThroughDataAgain() {

        for (SpecialTransaction trans : setOfSpecialTransactions) {

            //System.out.printf("Transaction ID: %d, Transaction WRule ID: %d\n", trans.getTransactionID(), trans.getWRule().getRuleID());
                if (trans.getWRule().getCoveredCasesCorrectly()) {

                    if (trans.getCRule() != null) {
                        trans.getCRule().removeClassCasesCovered(trans.getTransactionClass());
                    }
                    trans.getWRule().addClassCasesCovered(trans.getTransactionClass());

                } else {
                    var wSet = allCoverRules(trans);
                    
                    for (Rule rule : wSet) {
                        //System.out.printf("Rule ID: %d\n", rule.getRuleID());
                        if (trans.getCRule() != null) {
                            SpecialRule replaceRule = new SpecialRule(trans.getTransactionID(), trans.getTransactionClass(), trans.getCRule());
                            rule.addToReplace(replaceRule);
                            //System.out.printf("Rule to Replace: %d\n", replaceRule.getCRule().getRuleID());
                        }
                        rule.addClassCasesCovered(trans.getTransactionClass());
                    }
                    for (Rule rule : wSet) {

                        if (!setOfCRulesWithHigherPrecedence.contains(rule)) {

                            setOfCRulesWithHigherPrecedence.add(rule);

                        }
                    }
                }
        }
    }

    private ArrayList<Rule> allCoverRules(SpecialTransaction specialTransaction) {
        ArrayList<Rule> wSet = new ArrayList<>();
        // since the ids are just consecutive integers from 0 to n
        var currTransaction = transactionList[specialTransaction.getTransactionID()];
        for (Rule cRule : setOfCRules) {
            // if the cRule has a higher precedence
            if ( specialTransaction.getCRule() == null || cRule.compareTo(specialTransaction.getCRule()) > 0) {
                var match = true;
                // check if it wrongly classifies
                for (int item : currTransaction.getTransactionItems()) {
                   if (!ArrayUtils.contains(cRule.getAntecedent(), item)) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    if (cRule.getConsequent() != currTransaction.getTransactionClass()) {
                        wSet.add(cRule);
                    }
                }

            } else {
                break;
            }
        }
        return wSet;
    }

    // stage 3
    public ArrayList<Rule> chooseFinalRules() {
   
        var classDistr = compClassDistri(transactionList);
        int ruleErrors = 0;
        int ruleErrorsSoFar = 0;
        int totalErrors = 0;
        // line 3
        Collections.sort(setOfCRulesWithHigherPrecedence);
        //System.out.printf("Set of C Rules with higher precedence: %d\n", setOfCRulesWithHigherPrecedence.size());
        // line 4
        for (Rule rule : setOfCRulesWithHigherPrecedence) {
            var ruleClass = rule.getConsequent();
            //System.out.printf("Rule ID: %d, Rule Consequent: %d\n", rule.getRuleID(), ruleClass);
            // line 5
            if (rule.getClassCasesCovered().get(ruleClass) != 0) {
                // line 6
                //System.out.printf("Number of rules to replace: %d\n", rule.getReplace().size());
                for (SpecialRule replaceRule : rule.getReplace()) {
                    // if the transactionID has been covered
                    //System.out.printf("Rule to be replaced: %d\n", replaceRule.getTransactionID());
                    if (coveredTransaction.contains(replaceRule.getTransactionID())) {
                        rule.removeClassCasesCovered(replaceRule.getTransactionClass());
                    } else {
                        replaceRule.getCRule().removeClassCasesCovered(replaceRule.getTransactionClass());
                        //System.out.printf("replaceRule.getCRule: %d\n", replaceRule.getCRule().getRuleID());
                        for (Rule rule1 : setOfCRulesWithHigherPrecedence) {
                            if (replaceRule.getCRule().getRuleID() == rule1.getRuleID()) {
                                rule.removeClassCasesCovered(replaceRule.getTransactionClass());
                            }
                        }
                    }
                }

                ruleErrors = rule.errorsOfRule();
                ruleErrorsSoFar += ruleErrors;
                // ruleErrors += errorsOfRule(classDistr, rule);
                //System.out.printf("errors of rule: %d\n", ruleErrors);
                //System.out.printf("errors due to rules so far: %d\n", ruleErrorsSoFar);
                //System.out.printf("Rule ID:%d, Rule Consequent:%d, Rule Confidence: %f, Rule Antecedents:", rule.getRuleID(), rule.getConsequent(), rule.getConfidence());
                /* for (int items : rule.getAntecedent()) {
                    System.out.printf("%d ", items);
                }
                System.out.printf("\n"); */
                // there is a problem with classDistr
                classDistr = updateClassDistr(rule, classDistr);
                var defaultClass = selectDefault(classDistr);
                //System.out.printf("default class is %d\n", defaultClass);
                var defaultErrors = defErr(defaultClass, classDistr);
                //System.out.printf("default error is %d\n", defaultErrors);
                totalErrors = ruleErrorsSoFar + defaultErrors;
                //System.out.printf("total error is %d\n", totalErrors);
                // TODO: everything from 267 onwards
                ClassificationRule classificationRule = new ClassificationRule(rule, defaultClass, totalErrors);
                classifierRules.add(classificationRule);
            }
        }

        int minError = 99999;
        int min = 0;

        // line 18
        for (int i = 0; i < classifierRules.size(); i++) {
            if (classifierRules.get(i).getTotalError() < minError) {
                minError = classifierRules.get(i).getTotalError();
                min = i;
            }
        }
        //System.out.printf("Min Total Error:%d\n", minError);

        // line 19
        finalDefaultClass = classifierRules.get(min).getDefaultClass();
        // not sure how to add it to the end of C
        // TODO #11
        for (int i = 0; i <= min; i++) {
            finalClassifier.add(classifierRules.get(i).getRule());
        }

        return finalClassifier;
        
    }

    // count the number of training cases in each class
    // in the initial training data
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

    private HashMap<Integer, Integer> updateClassDistr(Rule rule, HashMap<Integer, Integer> classDistr) {
        var newClassDistr = new HashMap<Integer, Integer>();
        var classCasesCovered = rule.getClassCasesCovered();
        //System.out.printf("Size of classCasesCovered: %d\n", classCasesCovered.size());
        for (Entry<Integer, Integer> entry: classDistr.entrySet()) {
            var transactionClass = entry.getKey();
            var count = entry.getValue();
            //System.out.printf("Old Transaction Class:%d, Old Count:%d\n", transactionClass, count);
            if (rule.getClassCasesCovered().get(transactionClass) != null) {
                newClassDistr.put(transactionClass, count - rule.getClassCasesCovered().get(transactionClass));
            }
            else {
                newClassDistr.put(transactionClass, count);
            }
            //System.out.printf("New Transaction Class:%d, New Count:%d\n", transactionClass, newClassDistr.get(transactionClass));
            
        }
        // for (Entry<Integer, Integer> entry: classCasesCovered.entrySet()) {
        //     var transactionClass = entry.getKey();
        //     var count = entry.getValue();
        //     var currCount = classDistr.get(transactionClass);
        //     newClassDistr.put(transactionClass, currCount - count);
        // }
    return newClassDistr;
    }

    private int selectDefault(HashMap<Integer, Integer> classDistr) {
        int maxClass = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : classDistr.entrySet()) {
            Integer transClass = entry.getKey();
            Integer transCount = entry.getValue();
            if (transCount > maxCount) {
                maxClass = transClass;
                maxCount = transCount;
            }
        }

        return maxClass;
    }

    private int defErr(int defaultClass, HashMap<Integer, Integer> classDistr) {
        int error = 0;
        for (Map.Entry<Integer, Integer> entry : classDistr.entrySet()) {
            if (entry.getKey() != defaultClass) {
                //System.out.printf("Class %d, num:%d\n", entry.getKey(), entry.getValue());
                error += entry.getValue();
            }
        }
        return error;
    }

}

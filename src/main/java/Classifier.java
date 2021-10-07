import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.*;

public class Classifier{
    private ArrayList<Rule> sortedRuleArray = new ArrayList<>();
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
    private int finalDefaultClass;


    public void start() {
        sortedRuleArray = RG.getRuleArray();
        Collections.sort(sortedRuleArray);
    }
    // CBA-CB M2 Stage 1
    public void findCRuleAndWRule() {
        var transactionList = RG.getTransactionList();
        for (Transaction transaction : transactionList) {
            var transactionItems = transaction.getTransactionItems();
            var hasCRule = false;
            var hasWRule = false;
            // if (transaction.getCRule() == null || transaction.getWRule() == null) {
                for (Rule rule : sortedRuleArray) {
                    var ruleLHS = rule.getAntecedent();
                    var match = true;
                    for (int ruleItem : ruleLHS) {
                        if (!ArrayUtils.contains(transactionItems, ruleItem)) {
                            match = false;
                        }
                    }
                    if (match) {
                        // System.out.print(rule.getConsequent());
                        // System.out.print(", transaction class: ");
                        // System.out.print(transaction.getTransactionClass());
                        // System.out.print("\n");
                        // System.out.print("rule consequent:"); 
                        if (rule.getConsequent() == transaction.getTransactionClass()) {
                            if(!hasCRule) {
                                transaction.setCRule(rule);
                                setOfCRules.add(rule);
                                rule.addClassCasesCovered(transaction.getTransactionClass());
                                hasCRule = true;
                            }
                        } else {
                            if(!hasWRule) {
                            transaction.setWRule(rule);
                            hasWRule = true;
                            }
                        }
                    }
                }
            //} 
            if (sortedRuleArray.indexOf(transaction.getCRule()) < sortedRuleArray.indexOf(transaction.getWRule())) {
                // need to mark CRule
                System.out.println(setOfCRulesWithHigherPrecedence);
                setOfCRulesWithHigherPrecedence.add(transaction.getCRule());
                transaction.getCRule().markRule();

            } else {
                SpecialTransaction specialTransaction = new SpecialTransaction(transaction.getTransactionID(), transaction.getTransactionClass(), transaction.getCRule(), transaction.getWRule());
                setOfSpecialTransactions.add(specialTransaction);
            }
            // if cRule > wRule, need to keep a data structure containing:
            // transactionID, transactionClass, cRule, and wRule
        }
    }

    // CBA-CB: M2 (Stage 2)
    public void goThroughDataAgain() {
        for (SpecialTransaction trans : setOfSpecialTransactions) {
            if (trans.getWRule().getCoveredCasesCorrectly()) {
                trans.getCRule().removeClassCasesCovered(trans.getTransactionClass());
                trans.getWRule().addClassCasesCovered(trans.getTransactionClass());
            } else {
                var wSet = allCoverRules(trans);
                for (Rule rule : wSet) {
                    SpecialRule replaceRule = new SpecialRule(trans.getTransactionID(), trans.getTransactionClass(), trans.getCRule());
                    rule.addToReplace(replaceRule);
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
        ArrayList<Rule> wSet = new ArrayList<Rule>();
        // since the ids are just consecutive integers from 0 to n
        var currTransaction = RG.getTransactionList()[specialTransaction.getTransactionID()];
        // for (Transaction transaction : RG.getTransactionList()) {
        //     if (transaction.getTransactionID() == specialTransaction.getTransactionID()) {
        //         Transaction currTransaction = transaction;
        //         break;
        //     }
        // }
        for (Rule cRule : setOfCRules) {
            // if the cRule has a higher precedence
            // if (comparePrecedence(cRule, specialTransaction.getCRule()) == cRule) 
            if (cRule.compareTo(specialTransaction.getCRule()) > 0) {
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
    public void chooseFinalRules() {
        var transactionList = RG.getTransactionList();
        var classDistr = compClassDistri(transactionList);
        int ruleErrors = 0;
        // line 3
        Collections.sort(setOfCRulesWithHigherPrecedence);
        // line 4
        for (Rule rule : setOfCRulesWithHigherPrecedence) {
            var ruleClass = rule.getConsequent();
            // line 5
            if (rule.getClassCasesCovered().get(ruleClass) != 0) {
                // line 6
                for (SpecialRule replaceRule : rule.getReplace()) {
                    // if the transactionID has been covered
                    if (coveredTransaction.contains(replaceRule.getTransactionID())) {
                        rule.removeClassCasesCovered(replaceRule.getTransactionClass());
                    } else {
                        //FIXME #10
                        replaceRule.getCRule().removeClassCasesCovered(replaceRule.getTransactionClass());
                        Rule tempRule = replaceRule.getCRule();
                        // change set for which rule has to be edited if necessary
                        for (Rule rule1 : setOfCRulesWithHigherPrecedence){
                            if (rule1 == tempRule) {
                                rule1.removeClassCasesCovered(replaceRule.getTransactionClass());
                            }

                        }

                    }
                }
                ruleErrors += rule.errorsOfRule();
                classDistr = updateClassDistr(rule, classDistr);
                var defaultClass = selectDefault(classDistr);
                var defaultErrors = defErr(defaultClass, classDistr);
                var totalErrors = ruleErrors + defaultErrors;
                ClassificationRule classificationRule = new ClassificationRule(rule, defaultClass, totalErrors);
                classifierRules.add(classificationRule);
            }
        }
        int min = 9999;
        int lastClass = 0;
        // line 18
        for (int i = 0; i < classifierRules.size(); i++) {
            if (classifierRules.get(i).getTotalError() < min) {
                min = classifierRules.get(i).getTotalError();
            } else {
                lastClass = i - 1;
                break;
            }
        }

        // line 19
        finalDefaultClass = classifierRules.get(lastClass).getDefaultClass();
        // not sure how to add it to the end of C
        // TODO #11
        for (int j = 0; j <= lastClass; j++) {
            finalClassifier.add(classifierRules.get(j).getRule());
        }
        

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
        for (Entry<Integer, Integer> entry: classCasesCovered.entrySet()) {
            var transactionClass = entry.getKey();
            var count = entry.getValue();
            var currCount = classDistr.get(transactionClass);
            newClassDistr.put(transactionClass, currCount - count);
        }
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
                error += entry.getValue();
            }
        }
        return error;
    }

}

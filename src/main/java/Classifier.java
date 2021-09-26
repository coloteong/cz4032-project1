import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
import org.apache.commons.lang3.*;

public class Classifier{
    //TODO #2 implement the classifier class - at least init with attribute
    private ArrayList<Rule> sortedRuleArray;
    // this is set U in the paper
    private List<Rule> setOfCRules;
    // this is set Q in the paper
    private List<Rule> setOfCRulesWithHigherPrecedence;
    // this is set A in the paper
    private List<SpecialTransaction> setOfSpecialTransactions;



    public Rule comparePrecedence(Rule r1, Rule r2) {
        // 1. comparing confidence
        if (r1.getConfidence() > r2.getConfidence()) { return r1;}
        else if (r2.getConfidence() > r1.getConfidence()) { return r2; }
        // 2. comparing support
        else if (r1.getSupport() > r2.getSupport()) { return r1; } 
        else if (r2.getSupport() > r1.getSupport()) { return r2; }
        // 3. comparing which was generated first
        //FIXME #5
        else if (ArrayUtils.indexOf(RG.getRuleArray(), r1) < ArrayUtils.indexOf(RG.getRuleArray(), r2)) { return r1; }
        else { return r2; }
    }

    private void sortRules() {
        for (int i = 0; i < RG.getRuleArray().length - 1; i++){
            new Rule betterRule = comparePrecedence(ruleArray., r2);

        }
    }

    private void findCRuleAndWRule() {
        var transactionList = RG.getTransactionList();
        for (Transaction transaction : transactionList) {
            var transactionItems = transaction.getTransactionItems();
            if (!(transaction.getCRule() != null && transaction.getWRule() != null)) {
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
                                // mark rule to indicate that it classifies a case correctly
                                rule.incrementTransactionsCovered();
                            }
                        } else {
                            if(transaction.getWRule() == null) {
                            transaction.setWRule(rule);
                            }
                        }
                    }
                }
            }
            // if cRule > wRule, need to keep a data structure containing:
            // transactionID, transactionClass, cRule, and wRule
            if (sortedRuleArray.indexOf(transaction.getCRule()) < sortedRuleArray.indexOf(transaction.getWRule())) {
                // need to mark CRule
                setOfCRulesWithHigherPrecedence.add(transaction.getCRule());
                transaction.getCRule().markRule();

            } else {
                SpecialTransaction specialTransaction = new SpecialTransaction(transaction.getTransactionID(), transaction.getTransactionClass(), transaction.getCRule(), transaction.getWRule());
                setOfSpecialTransactions.add(specialTransaction);
            }
        }
    }

}
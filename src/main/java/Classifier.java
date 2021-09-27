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

    // CBA-CB M2 Stage 1
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
                                rule.addClassCasesCovered(transaction.getTransactionClass());
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

    // CBA-CB: M2 (Stage 2)
    private void goThroughDataAgain() {
        for (SpecialTransaction trans : setOfSpecialTransactions) {
            if (trans.getWRule().getCoveredCasesCorrectly()) {
                trans.getCRule().removeClassCasesCovered(trans.getTransactionClass());
                trans.getWRule().addClassCasesCovered(trans.getTransactionClass());
            } else {
                var wSet = allCoverRules(trans);
                for (Rule rule : wSet) {
                    rule.addToReplace(trans.getCRule());
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
            if (comparePrecedence(cRule, specialTransaction.getCRule()) == cRule) {
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

}

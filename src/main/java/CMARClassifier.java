import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class CMARClassifier {

    private List<Rule> sortedRuleArray = new ArrayList<>();
    private Transaction[] transactionList;
    private Map<Integer, Integer> classDistr = new HashMap<>();

    public CMARClassifier(List<Rule> ruleArray, Transaction[] transactionList) {
        sortedRuleArray = ruleArray;
        this.transactionList = transactionList;
        classDistr = compClassDistri(transactionList);
    }

    public void start() {
        Collections.sort(sortedRuleArray);
        for (Rule rule : sortedRuleArray) {
            System.out.printf("Rule ID:%d, Rule Confidence:%f, Rule Support:%f\n", rule.getRuleID(), rule.getConfidence(), rule.getSupport());
        }
    }

    public void findClassifier() {
        int error = 0;
        for (Transaction transaction : transactionList) {
            System.out.printf("Transaction ID:%d \n", transaction.getTransactionID());
            int classChosenByRules;
            List<Rule> ruleSubset = findRules(transaction);
            if (allSame(ruleSubset)) {
                // we use the class 
                classChosenByRules = ruleSubset.get(0).getConsequent();
            } else {
                // Map<Integer, List<Rule>> ruleGroups = new HashMap<>();
                Map<Integer, Float> groupWeight = new HashMap<>();
                for (Rule rule : ruleSubset) {
                    // ruleGroups.get(rule.getConsequent()).add(rule);
                    // calculate the max chi squared for each rule
                    var currClass = rule.getConsequent();
                    float maxChiSquared = calculateMaxChiSquared(rule);
                    float chiSquared = calculateChiSquared(rule);
                    float ruleValue = (chiSquared * chiSquared) / maxChiSquared;

                    if (!groupWeight.containsKey(rule.getConsequent())) {
                        groupWeight.put(currClass, ruleValue);
                    } else {
                        groupWeight.put(currClass, groupWeight.get(currClass) + ruleValue);
                    }
                }

                Map.Entry<Integer, Float> maxEntry = null;

                for (Map.Entry <Integer, Float> entry: groupWeight.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                }

                classChosenByRules = maxEntry.getKey();
            }
            if (classChosenByRules != transaction.getTransactionClass()) {
                error++;
            }

            System.out.printf("Current Error:%d\n", error);
        }
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

    private float calculateMaxChiSquared(Rule rule) {
        float supP = rule.getCondSupportCount();
        float supC = classDistr.get(rule.getConsequent());
        var t = transactionList.length;
        return (float) (Math.pow(Math.min(supC, supP) - ((supP * supC) / t), 2) * t * calculateE(rule));
    }

    private float calculateE(Rule rule) {
        float supP = rule.getCondSupportCount();
        float supC = classDistr.get(rule.getConsequent());
        var t = transactionList.length;
        float firstElement = 1 / (supP * supC);
        float secondElement = 1 / (supP * (t - supC));
        float thirdElement = 1 / ((t - supP) * supC);
        float fourthElement = 1 / ((t - supP) * (t - supC));
        return firstElement + secondElement + thirdElement + fourthElement;
    }

    private boolean allSame(List<Rule> ruleSubset) {
        var allSame = true;
        if (!ruleSubset.isEmpty()) {
            var ruleClass = ruleSubset.get(0).getConsequent();
            for (Rule rule : ruleSubset) {
                if (rule.getConsequent() != ruleClass) {
                    allSame = false;
                    return allSame;
                }
            }
        }
        return allSame;
    }

    public List<Rule> findRules(Transaction transaction) {
        List<Rule> matchedRule = new ArrayList<>();
        var transactionItems = transaction.getTransactionItems();
        for (Rule rule : sortedRuleArray) {
            var match = true;
            for (int ruleItem : rule.getAntecedent()) {
                if (!ArrayUtils.contains(transactionItems, ruleItem)) {
                    match = false;
                }
            }
            if (match) {
                matchedRule.add(rule);
            }
        }

        return matchedRule;
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

}

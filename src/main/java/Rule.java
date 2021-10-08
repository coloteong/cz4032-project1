import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;

public class Rule implements Comparable<Rule>{
    private int ruleID;
    private int[] antecedent;
    private int consequent;
    private int ruleSupportCount;
    private int condSupportCount;
    // needed in CBA-CB M2
    private HashMap<Integer, Integer> classCasesCovered = new HashMap<>();
    private boolean coveredCasesCorrectly;
    private ArrayList<SpecialRule> replace = new ArrayList<>();
    private static int count = 0;

    public Rule(int[] antecedent, int consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        ruleID = count;
        count++;
    }

    public Rule(int[] antecedent, int consequent, int condSup, int ruleSup) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        condSupportCount = condSup;
        ruleSupportCount = ruleSup;
        ruleID = count;
        count++;
    }


    public void setConsequent(int consequent) {
        this.consequent = consequent;
    }

    public ArrayList<SpecialRule> getReplace() {
        return replace;
    }

    public HashMap<Integer, Integer> getClassCasesCovered() {
        return classCasesCovered;
    }

    public int getRuleID() {
        return ruleID;
    }

    public int getRuleSupportCount() {
        return ruleSupportCount;
    }
    
    public int getCondSupportCount() {
        return condSupportCount;
    }

    public double getConfidence() {
        return ((double) ruleSupportCount / condSupportCount);
    }
    
    public double getSupport() {
        return ((double) ruleSupportCount / RG.numTransactions);
    }

    public int[] getAntecedent() {
        return this.antecedent;
    }

    public int getConsequent() {
        return this.consequent;
    }

    public boolean getCoveredCasesCorrectly() {
        return coveredCasesCorrectly;
    }

    public void markRule() {
        coveredCasesCorrectly = true;
    }

    public void addClassCasesCovered(int transactionClass) {

        // TODO: TIM DISCUSS: why is classCasesCovered a hashmap? shouldn't each rule only have one term in the consequent?
        // JETH RESPONDS: we talked about this already... each rule will only have one term in the consequent
        // but iif this rule has a high precedence, it may be used to classify things wrongly,
        // so in that sense, it covers that class. Look at pages 4 and 5 of the paper
        if (classCasesCovered.get(transactionClass) == null) {
            classCasesCovered.put(transactionClass, 1);
        } else {
            var currNum = classCasesCovered.get(transactionClass);
            classCasesCovered.put(transactionClass, currNum + 1);
        }
    }

    public void removeClassCasesCovered(int transactionClass) {
        var currNum = classCasesCovered.get(transactionClass);
        classCasesCovered.put(transactionClass, currNum - 1);
    }

    public void addToReplace(SpecialRule rule) {
        replace.add(rule);
    }

    public int errorsOfRule() {
        int error = 0;
        for (Entry<Integer, Integer> entry: classCasesCovered.entrySet()) {
            var transactionClass = entry.getKey();
            var count = entry.getValue();
            if (consequent != transactionClass) {
                error += count;
            }
        }
        return error;
    }

    @Override
    public int compareTo(Rule other) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than 
        // other and 0 if they are supposed to be equal
        if (confidence > other.getConfidence()) {
            return 1;
        } else if (confidence == other.getConfidence()) {
            if (support > other.getSupport()) {
                return 1;
            } else if (support == other.getSupport()) {
                if (ruleID < other.getRuleID() ) {
                    return 1;
                }
            }
        }
        return -1;
    }

    public void incrementRuleSupCount() {
        ruleSupportCount++;
    }

    public void incrementCondSupCount() {
        condSupportCount++;
    }

}


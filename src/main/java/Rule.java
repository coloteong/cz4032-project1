import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;

public class Rule implements Comparable<Rule>{
    private int ruleID;
    private int[] antecedent;
    private int consequent;
    private double confidence;
    private double support;
    // needed in CBA-CB M2
    private HashMap<Integer, Integer> classCasesCovered = new HashMap<>();
    private boolean coveredCasesCorrectly;
    private ArrayList<SpecialRule> replace = new ArrayList<>();
    private static int count = 0;

    public Rule(int[] antecedent, int consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.confidence = countConfidence(this.antecedent, this.consequent);
        this.support = RG.countSupport(this.antecedent);
        ruleID = count;
        count++;
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

    public double getConfidence() {
        return this.confidence;
    }
    
    public double getSupport() {
        return this.support;
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

    private double countConfidence(int[] antecedent, int consequent) { 
        var allElements = Arrays.copyOf(antecedent, antecedent.length + 1);
        allElements[allElements.length - 1] = consequent;
        support = RG.countSupport(allElements);
        double lhsSupport = RG.countSupport(antecedent);

        return support/lhsSupport;
    }

    public void markRule() {
        coveredCasesCorrectly = true;
    }

    public void addClassCasesCovered(int transactionClass) {
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


}


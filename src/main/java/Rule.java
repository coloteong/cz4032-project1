import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

public class Rule {
    private int[] antecedent;
    private int consequent;
    private double confidence;
    private double support;
    // needed in CBA-CB M2
    private Hashtable<Integer, Integer> classCasesCovered;
    private boolean coveredCasesCorrectly;
    private ArrayList<Rule> replace = new ArrayList<>();

    public Rule(int[] antecedent, int consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.confidence = countConfidence(this.antecedent, this.consequent);
        this.support = RG.countSupport(this.antecedent);
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

    public void addToReplace(Rule rule) {
        replace.add(rule);
    }

}


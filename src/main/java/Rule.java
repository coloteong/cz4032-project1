import java.util.Arrays;
import java.util.List;

public class Rule {
    private int[] antecedent;
    private int consequent;
    private double confidence;
    private double support;

    public Rule(int[] antecedent, int consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;

        // should we make a setConfidence and setSupport method, and invoke in RG?
        confidence = countConfidence(antecedent, consequent);
    }

    public double getConfidence() {
        return confidence;
    }
    
    public double getSupport() {
        return support;
    }

    public int[] getAntecedent() {
        return antecedent;
    }

    public int getConsequent() {
        return consequent;
    }

    private double countConfidence(int[] antecedent, int consequent) { 
        var allElements = Arrays.copyOf(antecedent, antecedent.length + 1);
        allElements[allElements.length - 1] = consequent;
        support = RG.countSupport(allElements);
        double lhsSupport = RG.countSupport(antecedent);

        return support/lhsSupport;
    }

}


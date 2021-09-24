import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rule {
    private int[] antecedent;
    private int[] consequent;
    private double confidence;

    public Rule(int[] antecedent, int[] consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;

        // should we make a setConfidence and setSupport method, and invoke in RG?
        confidence = countConfidence(antecedent, consequent);
    }

    public double getConfidence() {
        return confidence;
    }

    private double countSupport(int[] items) {
        // implemented in RG, just import this over? 
        // or maybe can just calculate support
    }

    private double countConfidence(int[] antecedent, int[] consequent) { 
        List<int[]> arrList = Arrays.asList(antecedent);
        arrList.add(consequent);
        var allElements = arrList.toArray();
        double allSupport = countSupport(allElements);
        double lhsSupport = countSupport(antecedent);

        return allSupport/lhsSupport;
    }
}


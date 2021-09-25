import java.util.Arrays;
import java.util.List;

public class Rule {
    private int[] antecedent;
    private int[] consequent;
    private double confidence;
    private double support;

    public Rule(int[] antecedent, int[] consequent) {
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

    private double countConfidence(int[] antecedent, int[] consequent) { 
        List<int[]> arrList = Arrays.asList(antecedent);
        arrList.add(consequent);
        // change the object array to an int array
        var allElements = arrList.toArray();
        int length = allElements.length;
        int[] intArray = new int[length];
        System.arraycopy(allElements, 0, intArray, 0, length);

        support = RG.countSupport(intArray);
        double lhsSupport = RG.countSupport(antecedent);

        return support/lhsSupport;
    }

}


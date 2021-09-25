import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
import org.apache.commons.lang3.*;

public class Classifier{
    //TODO #2 implement the classifier class - at least init with attribute
    private ArrayList<Rule> sortedRuleArray;



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

}
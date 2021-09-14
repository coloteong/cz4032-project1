import java.io.*;
import java.util.*;

public class RG {

    // list of the current itemsets
    private List<int[]> itemsets;
    // the file where the itemsets are located in
    private String sourceFile;
    // the number of itemsets
    private int numItems;
    // the number of transactions in the source file
    private int numTransactions;
    private double minSup;
    private double minConf;

    private void createInitialItemsets() {
        itemsets = new ArrayList<int[]>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i};
            itemsets.add(cand);
        }
    }

    private void calculateFrequentItemsets() {
        List<int[]> frequentCandidates = new ArrayList<int[]>();
        int count[] = new int[itemsets.size()];
        boolean[] records = new boolean[numItems];

        for (int i = 0; i < numTransactions; i++) {
            for (int j = 0; j < itemsets.size(); j++) {
                match = True;
                // tokenize the candidate
                int[] cand = itemsets.get(j);
                // check each item in the itemset to see if it is present
                for (int c : cand) {
                    if (trans[c] == false) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    count[j]++;
                }
            }
        }

        for (int i = 0; i < itemsets.size(); i++) {
            // add to the frequent candidates
            if ((count[i] / (double) (numTransactions)) >= minSup) {
                frequentCandidates.add[itemsets.get(i)]
            }
        }
        itemsets = frequentCandidates;
    }

}
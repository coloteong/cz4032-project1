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
        // itemsets will be an int of all of the items
        itemsets = new ArrayList<int[]>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i};
            itemsets.add(cand);
        }
    }

    private void calculateFrequentItemsets() {
        // the frequent candidate itemsets is an arraylist
        List<int[]> frequentCandidates = new ArrayList<int[]>();
        /*
         * create an int array of the size of the itemsets
         * create a boolean array of the size of the number of itemsets
         */
        int count[] = new int[itemsets.size()];
        boolean[] records = new boolean[numItems];

        // for each transaction
        for (int i = 0; i < numTransactions; i++) {
            // for each candidate itemset
            for (int j = 0; j < itemsets.size(); j++) {
                boolean match = True;
                int[] cand = itemsets.get(j);
                // tokenize the candidate
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
        // monotonicity
        itemsets = frequentCandidates;
    }

}
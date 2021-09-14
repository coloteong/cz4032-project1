import java.io.*;
import java.util.*;

public class RG {

    // list of the current itemsets
    public List<int[]> itemsets;
    // the file where the itemsets are located in
    public String sourceFile;
    // the number of itemsets
    public int numItems;
    // the number of transactions in the source file
    public int numTransactions;
    public double minSup;
    public double minConf;

    public void createInitialItemsets() {
        // itemsets will be an int of all the items

        itemsets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i};
            itemsets.add(cand);
        }
    }

    private void calculateFrequentItemsets() {
        // the frequent candidate itemsets is an arraylist
        List<int[]> frequentCandidates = new ArrayList<>();
        /*
         * create an int array of the size of the itemsets
         * create a boolean array of the size of the number of itemsets
         */
        int[] count = new int[itemsets.size()];
        boolean[] records = new boolean[numItems];

        // for each transaction
        for (int i = 0; i < numTransactions; i++) {
            // for each candidate itemset
            for (int j = 0; j < itemsets.size(); j++) {
                boolean match = true;
                int[] cand = itemsets.get(j);
                // tokenize the candidate
                for (int c : cand) {
                    if (!records[c]) {
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
                frequentCandidates.add(itemsets.get(i));
            }
        }
        // monotonicity
        itemsets = frequentCandidates;
    }

}
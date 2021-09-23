import de.viadee.discretizers4j.impl.EqualSizeDiscretizer;
import org.apache.commons.lang3.*;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class RG {

    // list of the current itemsets
    private List<int[]> itemsets;
    // the number of itemsets
    private int numItems;
    // the number of columns in the data
    private int numColumns;
    // the number of transactions in the source file
    private int numTransactions;
    private double minSup = 0.01;
    // path to the data file
    private String dataDir;
    // min confidence for all the itemsets
    private double minConf;
    private ArrayList<Integer> dataArray;
    // stores all the rules
    private ArrayList<Rule> ruleArray;

    private static final int NUMCLASSES = 3;


    public void start() {
        ArrayList<String> data = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        boolean fileFound = false;
        /*
        continually loop until a valid data file is read
         */
        while (!fileFound) {
            try {
                System.out.println("Type in directory to dataset (.data): ");
                dataDir = sc.nextLine();
                Scanner dataScanner = new Scanner(new File(dataDir));
                dataScanner.useDelimiter(",");
                // get rid of Header
                dataScanner.nextLine();
                while (dataScanner.hasNextLine()) {
                    String tempData = dataScanner.nextLine();
                    String[] tokens = tempData.split(",");
                    data.addAll(List.of(tokens));
                    numColumns = tokens.length;
                    numTransactions = data.size() / numColumns;
                }
                dataScanner.close();
                fileFound = true;
            } catch (Exception e) {
                System.out.println("File cannot be found");
            }
        }
// discretize the values in each column
        // need to have copiedValues since discretizer.fit sorts the array
        Double[][] values = new Double[numColumns][numTransactions];
        Double[][] copiedValues = new Double[numColumns][numTransactions];
        //TODO: currently i starts from 1 because the target is the 0th column
        // this needs to be changed when we change the data
        for (int i = 1; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                values[i][j] = Double.parseDouble(data.get((j * numColumns) + i));
                copiedValues[i][j] = values[i][j];
            }
            //TODO: #3 the current discretizer is the EqualSizeDiscretizer, can look for other libraries or other 
            // discretizers to implement a better algorithm
            EqualSizeDiscretizer discretizer = new EqualSizeDiscretizer();
            discretizer.fit(values[i]);
            copiedValues[i] = discretizer.apply(copiedValues[i]);
        }

        // convert the data to be the bin so that they are all ranging from 0 to the max in each column
        for (int i = 1; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                data.set((j * numColumns) + i, String.valueOf(copiedValues[i][j]));
            }
        }

        // make an Integer ArrayList
        ArrayList<Integer> intData = new ArrayList<>();
        for (String datum : data) {
            intData.add((int) (Float.parseFloat(datum)));
        }

        // convert the values of the data and put it in a 2d array
        Integer[][] intValues = new Integer[numColumns][numTransactions];
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                intValues[i][j] = (intData.get((j * numColumns) + i)); }
        }

        int maxColVal = Collections.max(Arrays.asList(intValues[0]));
        for (int i = 1; i < numColumns; i++) {
            int maxBin = Collections.max(Arrays.asList(intValues[i]));
            for (int j = 0; j < numTransactions; j++) {
                intData.set((j * numColumns) + i, intValues[i][j] + maxColVal);
            }
            maxColVal += maxBin;
        }

        numItems = (int) intData.stream().distinct().count();
        dataArray = intData;
        sc.close();
    }

    public void generateFrequentItemsets() {
        createInitialItemsets();
        int itemsetNumber = 1;
        //while (itemsets.size() > 0) {
        while (!itemsets.isEmpty()) {
            System.out.println("Itemsets.size: " + itemsets.size());
            calculateFrequentItemsets();
            if (!itemsets.isEmpty()) {
            // if (itemsets.size() != 0) {
                System.out.println("found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber);
                createNewItemsetsFromPrevious();
            }
            itemsetNumber++;
        }
    }

    public void generateAssocRulesFromItemsets() {
        //TODO #1 
        // this shouldn't be too complicated
        // we only care for those rules where the RHS is in 
        // the 
        int currentItemsetSize = itemsets.get(0).length;
        // for each itemset, we get a rule
        // let's use variant 2 of mining association rules from the lecture notes
        for (int[] itemset : itemsets) {
            int[] rightHandSide = {itemset[0]};
            // everything else is in the antecedent
            var leftHandSide = Arrays.copyOfRange(itemset, 1, itemset.length);
            Rule newRule = new Rule(leftHandSide, rightHandSide);
            // for this rule, we check if it is above the min confidence
            if (newRule.getConfidence() > minConf) {
                ruleArray.add(newRule);
                //TODO: actually, do we need to generate rules where
                // there are more than one elements on the RHS?
            }
        }

    }

    private void createNewItemsetsFromPrevious() {
        // get the number of items in the current candidate itemset
        int currentItemsetSize = itemsets.get(0).length;
        System.out.println("generating frequent candidate frequent itemsets of size " + (currentItemsetSize + 1) );

        HashMap<String, int[]> freqCandidates = new HashMap<>();

        for (int i = 0; i < itemsets.size(); i++) {
            for (int[] itemset : itemsets) {
                var X = itemsets.get(i);

                // using array X as the base, we make the first n - 1 elements of the next itemset
                // the elements of X
                int[] newCand = Arrays.copyOf(X, X.length);

                // we would then want to check for elements in the frequent n - 1 itemsets that are also frequent
                // but which has an element not in x
                int nDifferent = 0;
                for (int j : itemset) {
                    boolean found = false;

                    for (int x : X) {
                        if (x == j) {
                            found = true;
                            break;
                        }
                    }
                    // if there is such an element
                    // we add this to the last position of the new candidate itemset
                    if (!found) {
                        nDifferent++;
                        newCand[newCand.length - 1] = j;
                    }
                }
                // add this new frequent itemeset of length n
                // and put it into the freqCandidates list
                if (nDifferent == 1) {
                    Arrays.sort(newCand);
                    freqCandidates.put(Arrays.toString(newCand), newCand);
                }
            }
        }
        itemsets = new ArrayList<>(freqCandidates.values());
    }

    public void createInitialItemsets() {
        // itemsets will be an int of all the items

        itemsets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i + 1};
            itemsets.add(cand);
        }
    }


    private void calculateFrequentItemsets() {

        System.out.println("Calculating frequent itemsets to compute the frequency of " + itemsets.size() + " itemsets");
        List<int[]> frequentCandidates = new ArrayList<>();

        /*
          match: whether the transaction has al the items in an itemset
          count: number of successful matches
         */
        boolean match;
        int[] count = new int[itemsets.size()];

        // for each transaction
        for (int i = 0; i < numTransactions; i++) {
            // for each candidate itemset
            int[] transaction = new int[numColumns];
            for (int j = 0; j < numColumns; j++) {
                transaction[j] = dataArray.get((i * numColumns) + j);
            }
            for (int k = 0; k < itemsets.size(); k++) {
                match = true;
                int[] cand = itemsets.get(k);
                for (int c: cand) {
                    if (!ArrayUtils.contains(transaction, c)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    count[k]++;
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
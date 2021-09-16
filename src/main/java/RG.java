import de.viadee.discretizers4j.*;
import de.viadee.discretizers4j.impl.EqualSizeDiscretizer;

import java.io.*;
import java.util.*;

public class RG {

    // list of the current itemsets
    private List<int[]> itemsets;
    // the number of itemsets
    private int numItems;
    // the number of columns in the data
    private int numColumns;
    // the number of transactions in the source file
    private int numTransactions;
    private double minSup;
    // path to the data file
    private String dataDir;
    private double minConf;
    private ArrayList<Integer> dataArray;


    public void start() {
        ArrayList<String> data = new ArrayList<String>();
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
            // System.out.println(Arrays.toString(values[i]));
            EqualSizeDiscretizer discretizer = new EqualSizeDiscretizer();
            discretizer.fit(values[i]);
            // System.out.println(discretizer.getTransitions());
            copiedValues[i] = discretizer.apply(copiedValues[i]);
            // System.out.println(Arrays.toString(copiedValues[i]));
        }

        // convert the data to be the bin so that they are all ranging from 0 to the max in each column
        for (int i = 1; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                data.set((j * numColumns) + i, String.valueOf(copiedValues[i][j]));
            }
        }

        // make an Integer ArrayList
        ArrayList<Integer> intData = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            intData.add((int) (Float.parseFloat(data.get(i))));
        }

        // convert the values of the data and put it in a 2d array
        Integer[][] intValues = new Integer[numColumns][numTransactions];
        for (int i = 1; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                intValues[i][j] = (intData.get((j * numColumns) + i));
            }
        }

        for (int i = 1; i < numColumns; i++) {
            // bs code just to get the max value in each column
            int maxColVal = (int) Collections.max(Arrays.asList(intValues[i]));
            for (int j = 0; j < numTransactions; j++) {
                intData.set((j * numColumns) + i, (int) (copiedValues[i][j] + maxColVal * i));
            }
        }

        System.out.println("What is the min support");
        minSup = sc.nextFloat();
        numItems = (int) intData.stream().distinct().count();
        dataArray = intData;

        sc.close();
    }

    public void generateAssocRules() throws IOException {
        createInitialItemsets();
        int itemsetNumber = 1;
        int nbFrequentSets = 0;
        System.out.println("Itemset size: " + itemsetNumber);
        while (itemsets.size() > 0) {
            System.out.println("Itemsets.size: " + itemsets.size());
            calculateFrequentItemsets();
            if (itemsets.size() != 0) {
                nbFrequentSets += itemsets.size();
                System.out.println("found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber);
                createNewItemsetsfromPrevious();
            }
            itemsetNumber++;
        }
    }

    private void createNewItemsetsfromPrevious() {
    }

    public void createInitialItemsets() {
        // itemsets will be an int of all the items

        itemsets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i};
            itemsets.add(cand);
        }
    }

    private void readLineToBoolean(String transaction, boolean[] trans) {
        Arrays.fill(trans, false);
        // convert a transaction line into a tokenizer
        StringTokenizer transFile = new StringTokenizer(transaction, "");
        System.out.println(transFile.nextToken());
        while (transFile.hasMoreTokens()) {
            int tokenizedVal = Integer.parseInt(transFile.nextToken());
            trans[tokenizedVal] = true;
        }
    }

    private void calculateFrequentItemsets() throws IOException {

        System.out.println("Calculating frequent itemsets to compute the frequency of " + itemsets.size() + " itemsets");
        List<int[]> frequentCandidates = new ArrayList<>();

        /**
         * match: whether the transaction has al the items in an itemset
         * count: number of successful matches
         */
        boolean match;
        int[] count = new int[itemsets.size()];
        boolean[] trans = new boolean[numItems];
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir)));
        // get rid of headers
        dataIn.readLine();

        // for each transaction
        for (int i = 0; i < numTransactions; i++) {
            String transaction = dataIn.readLine();
            readLineToBoolean(transaction, trans);
            // for each candidate itemset
            for (int j = 0; j < itemsets.size(); j++) {
                match = true;
                int[] cand = itemsets.get(j);
                // tokenize the candidate
                for (int c : cand) {
                    if (!trans[c]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    count[j]++;
                }
            }
        }

        dataIn.close();
        System.out.println(count[0]);

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
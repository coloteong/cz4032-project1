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
    private float[][] dataArray;


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
/*
        dataArray = new float[numTransactions][numColumns];
        for (int i = 0; i < numTransactions; i++) {
            for (int j = 0; j < numColumns; j++) {
                dataArray[i][j] = Float.parseFloat(data.get((j * numTransactions) + i));
            }
        }
*/

        // TODO: discretize the values in each column

        // get unique values
        Set<String> uniqueValues = new HashSet<String>(data);
        // convert values to hashmap
        Map<Integer, Integer> valueMap = new HashMap<>();
        int i = 0;
        for (String value:
             uniqueValues) {
            valueMap.put(i, Integer.parseInt(value));
            i++;
        }


        System.out.println("What is the min support");
        minSup = sc.nextFloat();
        numItems = (int) data.stream().distinct().count();
        sc.close();
    }

    public void generateAssocRules() throws IOException {
        createInitialItemsets();
        int itemsetNumber = 1;
        int nbFrequentSets = 0;
        System.out.println("Itemset size: " + itemsetNumber);
        System.out.println(Arrays.toString(itemsets.get(0)));

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
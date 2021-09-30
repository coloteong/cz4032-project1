import de.viadee.discretizers4j.impl.EqualSizeDiscretizer;
import org.apache.commons.lang3.*;
import java.io.*;
import java.util.*;

import javax.sound.midi.Soundbank;

public class RG {

    // list of the current itemsets
    private List<int[]> itemsets;
    // the number of itemsets
    private int numItems;
    // the number of columns in the data
    private int numColumns;
    // the number of transactions in the source file
    private static int numTransactions;
    private double minSup = 0.01;
    // path to the data file
    private String dataDir;
    // min confidence for all the itemsets
    private double minConf;
    // the  2D array i.e. dataArray
    private static Transaction[] transactionList;
    public ArrayList<Integer> dataArray;
    // stores all the rules
    private static ArrayList<Rule> ruleArray = new ArrayList<>();

    // We might need this for... im not sure
    // private final int NUMCLASSES = 3;

    //TODO #8

    public static ArrayList<Rule> getRuleArray() {
        return ruleArray;
    }

    public static Transaction[] getTransactionList() {
        return transactionList;
    }


    public void start() {
        ArrayList<String> data = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        boolean fileFound = false;
        /*
        continually loop until a valid data file is read
         */
        var classColumn = 0;
        while (!fileFound) {
            try {
                System.out.println("Type in directory to dataset (.data): ");
                dataDir = sc.nextLine();
                Scanner dataScanner = new Scanner(new File(dataDir));
                dataScanner.useDelimiter(",");
                System.out.println("Column names:");

                String headerRow = dataScanner.nextLine();
                String[] headers = headerRow.split(",");
                System.out.println(headerRow);

                // boolean classFound = false;
                // while (!classFound) {
                //     System.out.println("Enter class name: ");
                //     String className = sc.nextLine();
                //     classIdx = Arrays.asList(headers).indexOf(className);
                //     if (classIdx != -1) {
                //         classFound = true;
                //     }
                // }

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
                boolean classFound = false;
                while (!classFound) {
                    System.out.println("Enter column number of class (0 indexed) ");
                    classColumn = sc.nextInt();
                    if (classColumn < numColumns && classColumn > -1)
                        classFound = true;
                }

                
            } catch (Exception e) {
                System.out.println("File cannot be found");
            }
        }
        var dataValues = fitDiscretizerToData(data, classColumn);
        // convert the data to be the bin so that they are all ranging from 0 to the max in each column
        for (int i = 1; i < numColumns; i++) {
            for (int j = 0; j < numTransactions; j++) {
                data.set((j * numColumns) + i, String.valueOf(dataValues[i][j]));
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
        convertToTransactionList(classColumn);
        sc.close();
    }

    //TODO #7
    public void generateFrequentItemsets() {
        createInitialItemsets();
        generateAssocRulesFromItemsets();
        pruneRules();
        int itemsetNumber = 1;
        while (!itemsets.isEmpty()) {
            System.out.println("Itemsets.size: " + itemsets.size());
            itemsets = calculateFrequentItemsets();
            System.out.println("Frequent Itemset size: " + itemsets.size());
            if (!itemsets.isEmpty()) {
                System.out.println("found " + itemsets.size() + " frequent itemsets of size " + itemsetNumber);
                itemsets = (List<int[]>) createNewItemsetsFromPrevious();
            }
            generateAssocRulesFromItemsets();
            System.out.println("Size of ruleArray: " + ruleArray.size());
            // TODO: #13 pruneRules takes too long
            pruneRules();
            System.out.println("Size of ruleArray: " + ruleArray.size());
            itemsetNumber++;
        }
    }

    private Double[][] fitDiscretizerToData(ArrayList<String> data, int classColumn) {
        // discretize the values in each column
        // need to have copiedValues since discretizer.fit sorts the array
        Double[][] values = new Double[numColumns][numTransactions];
        Double[][] copiedValues = new Double[numColumns][numTransactions];
        for (int i = 0; i < numColumns; i++) {
            if (i != classColumn) {
                for (int j = 0; j < numTransactions; j++) {
                    values[i][j] = Double.parseDouble(data.get((j * numColumns) + i));
                    copiedValues[i][j] = values[i][j];
                }
            //TODO: #3 the current discretizer is the EqualSizeDiscretizer, can look for other libraries or other 
            // discretizers to implement a better algorithm
            // System.out.println(values[i][0]);
            EqualSizeDiscretizer discretizer = new EqualSizeDiscretizer();
            discretizer.fit(values[i]);
            copiedValues[i] = discretizer.apply(copiedValues[i]);
            }
        }
        return copiedValues;
    }


    public void generateAssocRulesFromItemsets() {
        // for each itemset, we get a rule
        // let's use variant 2 of mining association rules from the lecture notes
        for (int[] itemset : itemsets) {
            int rightHandSide = itemset[0];
            // everything else is in the antecedent
            var leftHandSide = Arrays.copyOfRange(itemset, 1, itemset.length);
            Rule newRule = new Rule(leftHandSide, rightHandSide);
            // for this rule, we check if it is above the min confidence
            if (newRule.getConfidence() > minConf) {
                ruleArray.add(newRule);
            }
        }
    }


    //FIXME
    private Collection<int[]> createNewItemsetsFromPrevious() {
        // get the number of items in the current candidate itemset
        int currentItemsetSize = itemsets.get(0).length;
        System.out.println("generating frequent candidate frequent itemsets of size " + (currentItemsetSize + 1) );
        HashMap<String, int[]> freqCandidates = new HashMap<>();
        for (int i = 0; i < itemsets.size(); i++) {
            for (int j = 0; j < itemsets.size(); j++) {
                if (j != i) {
                    var X = itemsets.get(i);
                    // using array X as the base, we make the first n - 1 elements of the next itemset
                    // the elements of X
                    // int[] newCand = Arrays.copyOf(X, X.length);
                    int[] newCand = new int[X.length + 1];
                    for (int k = 0; k < X.length; k++) {
                        newCand[k] = X[k];
                    }
                    // we would then want to check for elements in the frequent n - 1 itemsets that are also frequent
                    // but which has an element not in x

                    int nDifferent = 0;
                    boolean found = false;
                    for (int item : itemsets.get(j)) {
                        for (int x : X) {
                            if (x == item) {
                                break;
                            } 
                            if (!found) {
                                nDifferent++;
                                var difference = item;
                            }
                        }
                        // if there is such an element
                        // we add this to the last position of the new candidate itemset
                        if (nDifferent == 1) {
                            newCand[newCand.length - 1] = item;
                            freqCandidates.put(Arrays.toString(newCand), newCand);
                        }
                    }
                    // add this new frequent itemeset of length n
                    // and put it into the freqCandidates list
            }
        }
    }
        return new ArrayList<int[]>(freqCandidates.values());
    }


    public void createInitialItemsets() {
        // itemsets will be an int of all the items

        itemsets = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            int[] cand = {i + 1};
            itemsets.add(cand);
        }
    }

    private void convertToTransactionList(int classColumn) {

        transactionList = new Transaction[numTransactions];
        for (int i = 0; i < numTransactions; i++) {
            int transactionClass = 0;
            int[] transactionItems = new int[numColumns - 1];
            for (int j = 0; j < numColumns; j++) {
                if (j == classColumn) {
                    transactionClass = dataArray.get((i * numColumns) + j);
                } else {
                    transactionItems[j - 1] = dataArray.get((i * numColumns) + j);
                }
            }
            Transaction transaction = new Transaction(transactionClass, transactionItems);
            transactionList[i] = transaction;
        }
    }

    private List<int[]> calculateFrequentItemsets() {
        var size = itemsets.get(0).length;

        System.out.println("Calculating frequent itemsets to compute the frequency of itemsets of size " + size);
        List<int[]> frequentCandidates = new ArrayList<>();

        // count support for each itemset 
        for (int i = 0; i < itemsets.size(); i++) {
            // add to the frequent candidates
            double support = countSupport(itemsets.get(i));
            if (support >= minSup) {
                frequentCandidates.add(itemsets.get(i));
            }
        }
        // monotonicity
        return frequentCandidates;
    }


    public static double countSupport(int[] items) {
        /*
          match: whether the transaction has al the items in an itemset
          count: number of successful matches
         */
        boolean match;
        int count = 0;

        // check items against each transaction
        for (int i = 0; i < numTransactions; i++) {
            match = true;
            // set match to false if there is an item from items is missing in transaction
            for (int c: items) {
                if (!ArrayUtils.contains(transactionList[i].getTransactionItems(), c)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                count++;
            }
        }
        return count / (double) (numTransactions);
    }

    public void pruneRules() {
        // prune the rules in the current Rule Array 
        ArrayList<Rule> currRuleArray = getRuleArray();
        for (Rule rule : currRuleArray) {
            var itemsInLHS = rule.getAntecedent().length;
            // we can only have an R^minus if the number of items on the LHS
            // is more than 1
            if (itemsInLHS > 1) {
                // for the number of items in the LHS = n
                // we can get n rules of size n - 1
                // using N choose N - 1
                for (int i = 0; i < itemsInLHS; i++) {
                    int[] newAntecedent = new int[itemsInLHS - 1];
                    // what we do is we choose which item to ignore i.e. i
                    for (int j = 0; j < newAntecedent.length; j++) {
                        if (j != i)
                            newAntecedent[j] = rule.getAntecedent()[i];
                        else
                            j--;
                    }
                    Rule rMinus = new Rule(newAntecedent, rule.getConsequent());
                    if (countPessimisticError(rule) > countPessimisticError(rMinus))
                        currRuleArray.remove(rule);
                }
            }
        }
    }

    private double countPessimisticError(Rule rule) {
        // since not all transactions will apply to a rule
        // we have to calculate the error based on the number 
        // of transactions that can be applied as well as 
        // the number of wrong transactions
        var numWrongTransactions = 0;
        var numApplicableTransactions = 0;
        for (Transaction transaction : transactionList) {
            var transactionClass = transaction.getTransactionClass();
            var transactionItems = transaction.getTransactionItems();
            var ruleLHS = rule.getAntecedent();
            var ruleClass = rule.getConsequent();
            var match = true;

            for (int item : transactionItems) {
                if (!ArrayUtils.contains(ruleLHS, item)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                numApplicableTransactions++;
                if (ruleClass != transactionClass)
                    numWrongTransactions++;
            }
        }
        double trainingError = numWrongTransactions / numApplicableTransactions;
        double pessimisticError = (numWrongTransactions + (rule.getAntecedent().length * 2)) / (double) numApplicableTransactions;
        return pessimisticError;
    }


}
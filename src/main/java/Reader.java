import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.apache.commons.lang3.ArrayUtils;

import de.viadee.discretizers4j.impl.EqualSizeDiscretizer;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Reader {

    public List<Transaction> transactionList;
    public String dataDir;
    public int numColumns;
    public int numTransactions;

    public void startReader() throws IOException, CsvException {

        Scanner sc = new Scanner(System.in);

        boolean fileFound = false;
        String fileName = "a";
        while (!fileFound) {
            System.out.println("Type in directory to dataset");
            fileName = sc.nextLine();
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory()) { 
                fileFound = true;
            }
        }

        boolean classFound = false;
        System.out.println("Enter column number of class (0 indexed) ");
        var classColumn = sc.nextInt();

        readCsv(fileName, classColumn);

    }

    public void readCsv(String fileName, Integer classColumn) throws IOException, CsvException {
        List<String[]> r;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            r = reader.readAll();
        }

        numColumns = r.get(0).length;
        numTransactions = r.size() - 1;
     
        List<String[]> itemList = new ArrayList<>();
        int[] classList = new int[numTransactions];

        for (int i = 1; i < numTransactions; i++) {
        // }
        // for(String[] arrays : r) {
            var arrays = r.get(i);
            String[] anotherArray = new String[arrays.length - 1];
            System.arraycopy(arrays, 0, anotherArray, 0, classColumn);
            System.arraycopy(arrays, classColumn + 1, anotherArray, classColumn, arrays.length - classColumn - 1);
            itemList.add(anotherArray);
            classList[i] = Integer.parseInt(arrays[classColumn]);
        }
        Double[][] dataValues = fitDiscretizerToData(itemList);
        Integer[][] intValues = new Integer[numColumns - 1][numTransactions];
        for (int i = 0; i < numColumns - 1; i++) {
            for (int j = 0; j < numTransactions; j++) {
                intValues[i][j] = dataValues[i][j].intValue();
            }
        }

        int maxColVal = Collections.max(Arrays.asList(ArrayUtils.toObject(classList)));
        for (int i = 0; i < numColumns - 1; i++) {
            int maxBin = Collections.max(Arrays.asList(intValues[i]));
            for (int j = 0; j < numTransactions; j++) {
                intValues[i][j] = intValues[i][j] + maxColVal;
            }
            maxColVal += maxBin;
        }

        for (int i = 0; i < numTransactions; i++) {
            int[] anotherArray = new int[numColumns - 1];
            for (int j = 0; j < numColumns - 1; j++) {
                anotherArray[j] = intValues[j][i];
            }
            Transaction transaction = new Transaction(classList[i], anotherArray);
            transactionList.add(transaction);
        }

        System.out.println(transactionList.get(2).getTransactionClass());

    }

    private Double[][] fitDiscretizerToData(List<String[]> data) {
        Double[][] values = new Double[numColumns - 1][numTransactions];
        Double[][] copiedValues = new Double[numColumns - 1][numTransactions];

        for (int i = 0; i < numColumns - 1; i++) {
            for (int j = 0; j < data.size(); j++) {
                var value = data.get(j)[i];
                values[i][j] = Double.parseDouble(value);
                copiedValues[i][j] = values[i][j];
                System.out.println(values[i][j]);
            }
            // FIXME
            EqualSizeDiscretizer discretizer = new EqualSizeDiscretizer();
            discretizer.fit(values[i]);
            copiedValues[i] = discretizer.apply(copiedValues[i]);
        }
    return copiedValues;
    }
}

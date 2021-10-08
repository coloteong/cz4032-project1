import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.apache.commons.lang3.ArrayUtils;

import de.viadee.discretizers4j.impl.EqualSizeDiscretizer;

import weka.core.*;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

public class Reader {

    public Transaction[] transactionList;
    public String dataDir;
    public int numColumns;
    public int numTransactions;

    public Transaction[] startReader() throws IOException, CsvException {

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

        System.out.println("Enter column number of class (0 indexed) ");
        var classColumn = sc.nextInt();
        readCsv(fileName, classColumn);
        sc.close();
        return transactionList;
    }

    public void readCsv(String fileName, Integer classColumn) throws IOException, CsvException {
        List<String[]> r;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            r = reader.readAll();
        }

        numColumns = r.get(0).length;
        numTransactions = r.size() - 1;
        for (int i = 1; i < numTransactions; i++) {
            var arrays = r.get(i)
             int[] anotherArray = new int[arrays.length - 1];
             System.arraycopy(arrays, 0, anotherArray, 0, classColumn);
             System.arraycopy(arrays, classColumn + 1, anotherArray, classColumn, arrays.length - classColumn - 1);
             Transaction transaction = new Transaction(Integer.parseInt(arrays[classColumn]), anotherArray);
             transactionList[i] = transaction;
        }
    }
}

    //     for (int i = 1; i < numTransactions; i++) {
    //     // }
    //     // for(String[] arrays : r) {
    //         var arrays = r.get(i);
    //         String[] anotherArray = new String[arrays.length - 1];
    //         System.arraycopy(arrays, 0, anotherArray, 0, classColumn);
    //         System.arraycopy(arrays, classColumn + 1, anotherArray, classColumn, arrays.length - classColumn - 1);
    //         Double[] copiedArray = new Double[anotherArray.length];
    //         for (int j = 0; j < copiedArray.length; j++) {
    //             copiedArray[j] = Double.parseDouble(anotherArray[j]);
    //         }
    //         itemList.add(copiedArray);
    //         classList[i] = Integer.parseInt(arrays[classColumn]);
    //     }
    //     Number[][] dataValues = fitDiscretizerToData(itemList);
    //     Integer[][] intValues = new Integer[numColumns - 1][numTransactions];
    //     for (int i = 0; i < numColumns - 1; i++) {
    //         for (int j = 0; j < numTransactions; j++) {
    //             intValues[i][j] = dataValues[i][j].intValue();
    //         }
    //     }

    //     int maxColVal = Collections.max(Arrays.asList(ArrayUtils.toObject(classList)));
    //     for (int i = 0; i < numColumns - 1; i++) {
    //         int maxBin = Collections.max(Arrays.asList(intValues[i]));
    //         for (int j = 0; j < numTransactions; j++) {
    //             intValues[i][j] = intValues[i][j] + maxColVal;
    //         }
    //         maxColVal += maxBin;
    //     }

    //     for (int i = 0; i < numTransactions; i++) {
    //         int[] anotherArray = new int[numColumns - 1];
    //         for (int j = 0; j < numColumns - 1; j++) {
    //             anotherArray[j] = intValues[j][i];
    //         }
    //         Transaction transaction = new Transaction(classList[i], anotherArray);
    //         transactionList.add(transaction);
    //     }

    //     System.out.println(transactionList.get(2).getTransactionClass());

    // }

    // private Number[][] fitDiscretizerToData(List<Double[]> data) {
    //     Number[][] values = new Double[numColumns - 1][numTransactions];
    //     Number[][] copiedValues = new Double[numColumns - 1][numTransactions];

    //     for (int i = 0; i < numColumns - 1; i++) {
    //     Number[] valuesToDiscretize = new Double[numTransactions];
    //         for (int j = 0; j < data.size(); j++) {
    //             valuesToDiscretize[j] = (Number) data.get(j)[i];
    //             // var value = data.get(j)[i];
    //             // values[i][j] = value;
    //             copiedValues[i][j] = valuesToDiscretize[j];
    //             // System.out.println(values[i]);
    //         }
    //         // FIXME
    //         EqualSizeDiscretizer discretizer = new EqualSizeDiscretizer();
    //         discretizer.fit(valuesToDiscretize);
    //         copiedValues[i] = discretizer.apply(copiedValues[i]);
    //     }
    // return copiedValues;
    // }
//}

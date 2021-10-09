import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class Reader {

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
        var transactionList = readCsv(fileName, classColumn);
        sc.close();
        return transactionList;
    }

    public Transaction[] readCsv(String fileName, Integer classColumn) throws IOException, CsvException {
        List<String[]> r;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            r = reader.readAll();
        }

        numColumns = r.get(0).length;
        numTransactions = r.size() - 1;
        var transactionList = new Transaction[numTransactions];
        for (int i = 0; i < numTransactions; i++) {
            // ignore header
             var arrays = r.get(i + 1);
             String[] anotherArray = new String[arrays.length - 1];
             int[] intArray = new int[arrays.length - 1];
             System.arraycopy(arrays, 0, anotherArray, 0, classColumn);
             System.arraycopy(arrays, classColumn + 1, anotherArray, classColumn, arrays.length - classColumn - 1);
             for (int j = 0; j < anotherArray.length; j++) {
                 intArray[j] = Integer.parseInt(anotherArray[j]);
             }
             Transaction transaction = new Transaction(Integer.parseInt(arrays[classColumn]), intArray);

             transactionList[i] = transaction;
        }
        return transactionList; 
    }
}
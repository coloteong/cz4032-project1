import java.io.*;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ArrayList<String> data = new ArrayList<String>();
        RG rg = new RG();
        Scanner sc = new Scanner(System.in);
        int numColumns = 0;
        boolean fileFound = false;
        /*
        continually loop until a valid data file is read
         */
        while (!fileFound) {
            try {
                System.out.println("Type in directory to dataset (.data): ");
                String dataDir = sc.nextLine();
                Scanner dataScanner = new Scanner(new File(dataDir));
                dataScanner.useDelimiter(",");
                while (dataScanner.hasNextLine()) {
                    String tempData = dataScanner.nextLine();
                    String[] tokens = tempData.split(",");
                    numColumns = tokens.length;
                    data.addAll(List.of(tokens));
                }
                dataScanner.close();
                fileFound = true;
            } catch (Exception e) {
                System.out.println("File cannot be found");
            }
        }
        // TODO: discretize the continuous values into bins
        rg.numTransactions = (data.size() / numColumns);
        rg.numItems = (int) data.stream().distinct().count();
        rg.createInitialItemsets();
        System.out.println(rg.numItems);
    }

}

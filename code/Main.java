import java.io.*;  
import java.util.ArrayList;
import java.util.Scanner;  

public class main {  
    public static void main(String[] args) throws Exception {
        ArrayList<String> data = new ArrayList<String>();
        Scanner sc = new Scanner(System.in);
        System.out.println("Type in directory to dataset (.csv): ");
        String dataDir = sc.nextLine();
        Scanner sc = new Scanner(new File(dataDir)); 
        sc.useDelimiter(",");
        while (sc.hasNext()) {
            data.add(sc.next());
        }
        sc.close();
    }
        // System.out.println("Dir is: " + dataDir);

}  
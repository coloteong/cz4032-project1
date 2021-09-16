import java.io.*;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        RG rg = new RG();
        rg.start();
        rg.generateAssocRules();
    }

}

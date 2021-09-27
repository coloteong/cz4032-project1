// Java implementation of QuickSort, from https://www.geeksforgeeks.org/quick-sort/
import java.io.*;
import org.apache.commons.lang3.ArrayUtils;
  
class QuickSort{
      
    // A utility function to swap two elements
    static void swap(Rule[] arr, int i, int j) {
        Rule temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    /* This function takes last element as pivot, places
    the pivot element at its correct position in sorted
    array, and places all smaller (smaller than pivot)
    to left of pivot and all greater elements to right
    of pivot */

    private static boolean comparePrecedence(Rule r1, Rule r2) {
        // 1. comparing confidence
        if (r1.getConfidence() > r2.getConfidence()) { return true;}
        else if (r2.getConfidence() > r1.getConfidence()) { return false; }
        // 2. comparing support
        else if (r1.getSupport() > r2.getSupport()) { return true; } 
        else if (r2.getSupport() > r1.getSupport()) { return false; }
        // 3. comparing which was generated first
        //FIXME #5
        else if (ArrayUtils.indexOf(RG.getRuleArray(), r1) < ArrayUtils.indexOf(RG.getRuleArray(), r2)) { return r1; }
        else { return false; }
    }

    static int partition(Rule[] arr, int low, int high) {
        
        // pivot
        Rule pivot = arr[high]; 
        
        // Index of smaller element and
        // indicates the right position
        // of pivot found so far
        int i = (low - 1); 
    
        for(int j = low; j <= high - 1; j++) {
            
            // If current element is smaller 
            // than the pivot
            if (comparePrecedence(arr[j], pivot) == false) {
                
                // Increment index of 
                // smaller element
                i++; 
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return (i + 1);
    }
    
    /* The main function that implements QuickSort
            arr[] --> Array to be sorted,
            low --> Starting index,
            high --> Ending index
    */
    static Rule[] quickSort(Rule[] arr, int low, int high) {
        if (low < high) {
            
            // pi is partitioning index, arr[p]
            // is now at right place 
            int pi = partition(arr, low, high);
    
            // Separately sort elements before
            // partition and after partition
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
        return arr;
    }
}
    
    
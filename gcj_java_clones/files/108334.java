package _2015_qualification;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 @SuppressWarnings("unused")
 public class SolutionB {
 
   // For example, "B-small". Download input/output files to TOP-LEVEL directory of project.
   private static final String FILENAME = "B-large";
 
   /*
    * API for output:
    * - void sout(String): output to console
    * - void fout(String): output to output file
    * - void out(String): output to console and output file
    * - void outC(int, Object): outputs "Case #X: " + obj.toString()
    * 
    * API for file reading:
    * - String[] getStrings(): read and parse one line of Strings from input file
    * - String getString(): read and parse one line of one String from input file
    * - int[] getInts(): read and parse one line of ints from input file
    * - int getInt(): read and parse one line of one int from input file
    * - double[] getDoubles(): read and parse one line of doubles from input file
    * - double getDouble(): read and parse one line of one double from input file
    */
   private void solveCase(int caseNumber) throws Exception {
     int D = getInt();
     int[] empties = getInts();
 
     List<Integer> uniques = new ArrayList<Integer>();
     Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
 
     for (int num : empties) {
       if (!counts.containsKey(num)) {
         uniques.add(num);
         counts.put(num, 0);
       }
       counts.put(num, counts.get(num) + 1);
     }
 
     Collections.sort(uniques, new Comparator<Integer>() {
       @Override
       public int compare(Integer arg0, Integer arg1) {
         return -arg0.compareTo(arg1);
       }
     });
 
     int max = uniques.get(0);
     int minCost = Integer.MAX_VALUE;
 
     for (int limit = max; limit >= 1; limit--) {
       int allSlices = 0;
       for (int i = 0; i < uniques.size(); i++) {
         int size = uniques.get(i);
         if (size <= limit) break;
         int slices = size / limit + (size % limit == 0 ? 0 : 1) - 1;
         int totalSlices = slices * counts.get(size);
         allSlices += totalSlices;
       }
       minCost = Math.min(minCost, limit + allSlices);
     }
 
 
     outC(caseNumber, minCost);
   }
 
 
   /* Code run from main() */
   public SolutionB() {
     int numCases = 0;
     try {
       rd = new BufferedReader(new FileReader(FILENAME + ".in"));
       wr = new PrintWriter(new FileWriter(FILENAME + ".out"));
       numCases = getInt();
     } catch (Exception e) {
       sout("Error reading file:");
       e.printStackTrace();
       return;
     }
 
     for(int i = 1; i <= numCases; i++) {
       try {
         solveCase(i);
       } catch (Exception e) {
         sout("Exception in Case " + i + ". Stack trace:");
         e.printStackTrace();
         break;
       }
     }
 
     try {
       rd.close();
       wr.close();
     } catch (Exception e) {
       sout("Error closing file:");
       e.printStackTrace();
       return;
     }
   }
 
   /* ----- Utility fields ----- */
 
   private BufferedReader rd = null;
   private PrintWriter wr = null;
 
   // Output helper methods
   private void sout(String s) { System.out.println(s); }
   private void fout(String s) { wr.println(s); }
   private void out(String s) { sout(s); fout(s); }
   private void outC(int c, Object s) { String out = "Case #" + c + ": " + s.toString(); sout(out); fout(out); }
 
   // File reading helper methods
   private String[] getStrings() throws Exception {
     return rd.readLine().split(" ");
   }
   private int[] getInts() throws Exception {
     String[] strData = getStrings();
     int[] arr = new int[strData.length];
     for(int i = 0; i < strData.length; i++) arr[i] = Integer.parseInt(strData[i]);
     return arr;
   }
   private double[] getDoubles() throws Exception {
     String[] strData = getStrings();
     double[] arr = new double[strData.length];
     for(int i = 0; i < strData.length; i++) arr[i] = Double.parseDouble(strData[i]);
     return arr;
   }
   private String getString() throws Exception {
     String[] strData = getStrings();
     if (strData.length != 1) throw new RuntimeException("Misinterpreting the file in getString().");
     return strData[0];
   }
   private int getInt() throws Exception {
     int[] intData = getInts();
     if (intData.length != 1) throw new RuntimeException("Misinterpreting the file in getInt().");
     return intData[0];
   }
   private double getDouble() throws Exception {
     double[] doubleData = getDoubles();
     if (doubleData.length != 1) throw new RuntimeException("Misinterpreting the file in getDouble().");
     return doubleData[0];
   }
 
   /* Start program via code in constructor */
   public static void main(String[] args) {
     new SolutionB();
   }
 }

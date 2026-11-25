package com.meme;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 /**
  * @author Prabakar Kalivaradan (Prabakar_Kalivaradan@Trimble.com)
  */
 public class Fractile {
   public static void main(String[] args) throws FileNotFoundException {
     if (args.length != 1) {
       System.out.println("Please input file name");
       return;
     }
     String filePath = args[0];
     Scanner scan = new Scanner(new File(filePath));
 
     int testCaseCount = Integer.valueOf(scan.nextLine());
     int i = 1;
     while (scan.hasNextLine()) {
       System.out.printf("");
       String[] kcs = scan.nextLine().split(" ");
       runFractile(i, Integer.valueOf(kcs[0]), Integer.valueOf(kcs[1]), Integer.valueOf(kcs[2]));
       i++;
     }
     System.out.println();
   }
 
   public static void runFractile(int testCase, int k, int c, int s) {
     System.out.println();
     String outputFormat = "Case #%d: %s";
     System.out.printf(outputFormat, testCase, getAllPositions(k, c, s));
   }
 
   private static String getAllPositions(int k, int c, int s) {
     if (s < k && c == 1) {
       return "IMPOSSIBLE";
     }
     String result = "";
     int init = 1;
     if (c!=1) {
       init = 2;
     }
     for (int i = init; i <= k; i++) {
       result += String.valueOf(i) + " ";
     }
     if(result.trim() == ""){
       return "1";
     }
     return result;
   }
 }

package com.amu.codejam.sixteen.qualification;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class Second {
 
 
     private static final String WORK_DIR = "/Users/amudhan/sandbox/codejam/inputs/2016/qualification/Second/";
     private static final char HAPPY = '+';
     private static final char SAD = '-';
 
     public static void main(String[] args) throws Exception {
         Second second = new Second();
         Scanner sc = new Scanner(new FileReader(WORK_DIR + "input-large.txt"));
         PrintWriter pw = new PrintWriter(new FileWriter(WORK_DIR + "output-large.txt"));
         int caseCnt = sc.nextInt();
         for (int caseNum=0; caseNum<caseCnt; caseNum++) {
             System.out.println("Processing test case " + (caseNum + 1));
             pw.print("Case #" + (caseNum+1) + ": ");
             second.solve(sc, pw);
         }
         pw.flush();
         pw.close();
         sc.close();
     }
 
     private void solve(Scanner sc, PrintWriter pw) {
         String pancakes = sc.next();
         int totalNumberOfPancakes = pancakes.length();
         char currentChar = pancakes.charAt(0);
         int currentLocation = 1;
         int swaps = 0;
         if (totalNumberOfPancakes == 1) {
             pw.println(currentChar == HAPPY ? 0 : 1);
             return;
         }
         while (currentLocation < totalNumberOfPancakes) {
             int nextAltLocation = pancakes.indexOf(alternateChar(currentChar), currentLocation);
             if (nextAltLocation == -1) {
                 pw.println(currentChar == HAPPY ? swaps : ++swaps);
                 return;
             }
             currentChar = alternateChar(currentChar);
             currentLocation = nextAltLocation;
             swaps++;
         }
     }
 
     private char alternateChar(char happyOrSad) {
         if (happyOrSad == HAPPY) {
             return SAD;
         } else {
             return HAPPY;
         }
     }
 }

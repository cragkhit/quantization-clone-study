package com.google.codejam.q2015;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 
 public class ProblemA {
 
     private static String PROBLEM_NAME = "/Users/danilova/dev/projects/misc/codejam/src/com/google/codejam/q2015/A-large";
     //private static String PROBLEM_NAME = "/Users/danilova/dev/projects/misc/codejam/src/com/google/codejam/q2015/A-small-attempt0.in";
     //private static String PROBLEM_NAME = "/Users/danilova/dev/projects/misc/codejam/test";
 
     public static void main(String[] args) throws IOException {
         try (BufferedReader reader = new BufferedReader(new FileReader(PROBLEM_NAME + ".in"));
             PrintStream out = new PrintStream(new FileOutputStream(PROBLEM_NAME + ".txt"))) {
             int t = Integer.parseInt(reader.readLine());
             for (int i = 1; i <= t; i++) {
                 solve(reader, out, i);
             }
         }
     }
 
     private static void solve(BufferedReader reader, PrintStream out, int caseN) throws IOException {
         String[] line = reader.readLine().split(" ");
         int smax = Integer.valueOf(line[0]);
         String audience = line[1];
         int standing = audience.charAt(0) - '0';
         int friends = 0;
         for (int ss = 1; ss < audience.length(); ss++) {
             if (ss > standing) {
                 int needMoreStands  = ss - standing;
                 friends += needMoreStands;
                 standing += needMoreStands;
             }
             standing += (audience.charAt(ss) - '0');
         }
 
 
         printResult(out, caseN, "" + friends);
     }
 
     private static void printResult(PrintStream out, int caseN, String result) {
         String str = "Case #" + caseN + ": " + result;
         System.out.println(str);
         out.println(str);
     }
 }

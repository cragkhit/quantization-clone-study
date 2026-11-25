package com.google.contest1a;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 public class A {
 
     private static final String INPUT = "/home/ckaratza/codejam2016/A/test-min.in";
     private static final String OUTPUT = "/home/ckaratza/codejam2016/A/test-min.out";
 
     public static void main(String[] args) throws FileNotFoundException {
         System.setIn(new FileInputStream(INPUT));
         System.setOut(new PrintStream(new FileOutputStream(OUTPUT)));
         Scanner in = new Scanner(System.in);
         int T = in.nextInt();
         for (int caseNum = 1; caseNum <= T; caseNum++) {
             String s = in.next();
             System.out.printf("Case #%d: %s\n", caseNum, producePhoneNumber(s));
         }
     }
 
     public static String producePhoneNumber(String s) {
         int[] occurences = new int[10];
         int[] sizes = new int[]{4, 3, 3, 5, 4, 4, 3, 5, 5, 4};
         Map<String, String> map = new HashMap<>();
         map.put("Z", "ZERO");
         map.put("W", "TWO");
         map.put("U", "FOUR");
         map.put("X", "SIX");
         map.put("G", "EIGHT");
 
         Map<String, Integer> map1 = new HashMap<>();
         map1.put("Z", 0);
         map1.put("W", 2);
         map1.put("U", 4);
         map1.put("X", 6);
         map1.put("G", 8);
 
         Map<String, String> map3 = new HashMap<>();
         map3.put("O", "ONE");
         map3.put("R", "THREE");
         map3.put("F", "FIVE");
         map3.put("S", "SEVEN");
 
 
         Map<String, Integer> map4 = new HashMap<>();
         map4.put("O", 1);
         map4.put("R", 3);
         map4.put("F", 5);
         map4.put("S", 7);
 
         for (String key : map.keySet()) {
             int indexOf = 0;
             while ((indexOf = s.indexOf(key, indexOf)) != -1) {
                 occurences[map1.get(key)]++;
                 indexOf++;
             }
         }
 
         for (String key : map3.keySet()) {
             int indexOf = 0;
             while ((indexOf = s.indexOf(key, indexOf)) != -1) {
                 occurences[map4.get(key)]++;
                 indexOf++;
             }
         }
 
         if (occurences[1] > 0) {
             occurences[1] -= (occurences[0] + occurences[2] + occurences[4]);
         }
 
         if (occurences[3] > 0) {
             occurences[3] -= (occurences[0] + occurences[4]);
         }
 
         if (occurences[5] > 0) {
             occurences[5] -= (occurences[4]);
         }
 
         if (occurences[7] > 0) {
             occurences[7] -= (occurences[6]);
         }
 
 
         long all = 0;
         for (int i = 0; i < occurences.length; i++) {
             all += occurences[i] * sizes[i];
         }
         occurences[9] = (int) ((s.length() - all) / 4);
 
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < occurences.length; i++) {
             for (int x = 1; x <= occurences[i]; x++) {
                 if (occurences[i] > 0)
                     sb.append(i);
             }
         }
         return sb.toString();
     }
 }

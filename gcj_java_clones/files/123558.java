package com.codejam;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 public class CountingSheep {
     public static void main(String[] args) throws FileNotFoundException {
         Scanner scanner = new Scanner(new File("/home/soj/workspace/interview/src/com/codejam/input.txt"));
         int numOfTest = 0;
         int caseNum = 0;
         while (scanner.hasNextLine()) {
             int t = Integer.valueOf(scanner.nextLine());
             if (numOfTest == 0) {
                 numOfTest = t;
             } else {
                 if (t > 0) {
                     int num = t;
                     int counter = 1;
                     int[] bitmap = new int[10];
                     int[] lastDigitMap = new int[10];
                     while (true) {
                         setBitMap(bitmap, lastDigitMap, num);
 
                         if (allBitSet(bitmap)) {
                             System.out.println("Case #" + caseNum + ": " + num);
                             break;
                         } else {
                             counter++;
                             num = counter * t;
                         }
                         if (allBitSet(lastDigitMap)) {
                             printInSomnia(caseNum);
                             break;
                         }
                     }
                 } else {
                     printInSomnia(caseNum);
                 }
 
             }
             caseNum++;
         }
     }
 
     private static boolean allBitSet(int[] bitmap) {
         for (int i = 0; i < 10; i++) {
             if (bitmap[i] == 0) {
                 return false;
             }
         }
         return true;
     }
 
     private static int setBitMap(int[] bitmap, int[] lastDigitMap, int num) {
         int digit = 0;
 
         while (num > 0) {
             digit = num % 10;
             num = num / 10;
             bitmap[digit] = 1;
         }
         lastDigitMap[digit] = 1;
         return digit;
     }
 
     private static void printInSomnia(int caseNum) {
         System.out.println("Case #" + caseNum + ": " + "INSOMNIA");
     }
 }
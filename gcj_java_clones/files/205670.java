package com.mypackage;
 
 import java.math.BigInteger;
 import java.util.*;
 import java.util.stream.Collectors;
 import java.util.stream.IntStream;
 
 /**
  * Created by Marcus on 4/8/2016.
  */
 public class Main {
 
     //Fractiles - failed
     public static void main(String... orange) throws Exception {
         Scanner input = new Scanner(System.in);
         int numCases = input.nextInt();
         for (int n = 0; n < numCases; n++) {
             int N = input.nextInt();
             int[] senatorList = new int[N];
             int max = 0;
             int maxValue = 0;
             int sum = 0;
             for(int i = 0; i<N; i++){
                 senatorList[i] = input.nextInt();
                 if(senatorList[i] > maxValue) {
                     maxValue = senatorList[i];
                     max = i;
                 }
                 sum += senatorList[i];
             }
             StringBuilder answerSB = new StringBuilder();
             while(sum>0){
                 answerSB.append(getAlphabetChar(max));
                 senatorList[max]--;
                 sum--;
                 if(sum == 0) break;
                 int[] maxes = checkMax(N, senatorList);
                 max = maxes[0];
                 maxValue = maxes[1];
                 sum--;
                 maxValue--;
                 if(sum ==0) {
                     senatorList[max]--;
                     answerSB.append(getAlphabetChar(max));
                     break;
                 }
                 else if((double)maxValue/(double)sum <=0.5 && sum > 1) {
                     senatorList[max]--;
                     answerSB.append(getAlphabetChar(max));
                 }
                 else{
                     sum++;
                     maxValue++;
                 }
                 answerSB.append(" ");
                 maxes = checkMax(N, senatorList);
                 max = maxes[0];
                 maxValue = maxes[1];
             }
 
             String answer = answerSB.toString();
             System.out.printf("Case #%d: %s\n", n + 1, answer);
 
         }
 
     }
 
     public static String getAlphabetChar(int i) {
         return String.valueOf((char)(i + 65));
     }
 
     public static int[] checkMax(int N, int[] valList){
         int[] maxes = new int[2];
         maxes[0] = 0; //max
         maxes[1] = 0; //maxValue
         for(int i = 0; i<N; i++) {
             if (valList[i] > maxes[1]) {
                 maxes[1] = valList[i];
                 maxes[0] = i;
             }
         }
         return maxes;
     }
 }

package com.codejam;
 
 import java.util.*;
 
 /**
  * Created by Mario on 4/16/2016.
  */
 public class RankFile extends Solver<String> {
 
 
     @Override
     public String solveCase() {
         int n = input.nextInt();
         Map<Integer, Integer> frequencies = new TreeMap<>();
         for (int ind = 0; ind < 2 * n - 1; ind++) {
             for (int x = 0; x < n; x++) {
                 int number = input.nextInt();
                 int count = 1;
                 if (frequencies.containsKey(number)) {
                     count += frequencies.get(number);
                 }
                 frequencies.put(number, count);
             }
         }
         SortedSet<Integer> keys = new TreeSet<Integer>(frequencies.keySet());
         List<Integer> missingNumbers = new ArrayList<>();
         keys.forEach(num -> {
             if (frequencies.get(num) % 2 == 1) {
                 missingNumbers.add(num);
             }
         });
         Collections.sort(missingNumbers);
         StringBuilder result = new StringBuilder();
         missingNumbers.forEach(num -> {
             result.append(num + " ");
         });
         return result.toString().substring(0, result.length() - 1);
     }
 
 }

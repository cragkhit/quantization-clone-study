package com.codejam;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Scanner;
 import java.util.stream.Stream;
 
 /**
  * Created by dx on 3/30/16.
  */
 public class Main2016TheLastWord {
 
     public static void main(String[] args) throws IOException {
         Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 
         Long t = Long.parseLong(in.nextLine());
         for (int i =1; i <= t; ++i) {
             String s = in.nextLine();
             String[] ss = s.split("", -1);
             String output = "";
 
             for(String l : ss) {
                 if(l.equals("")) continue;
                 if(output.length() == 0) {
                     output = l;
                 }
                 else {
                     String firstChar = output.substring(0,1);
                     if(firstChar.compareTo(l) > 0) {
                         output += l;
                     }
                     else {
                         output = l + output;
                     }
                 }
             }
 
             System.out.print("Case #" + i + ": " + output );
             System.out.println();
         }
     }
 
 }

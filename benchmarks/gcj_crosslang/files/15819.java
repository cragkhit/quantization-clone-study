package com.donguyen.googlecodejam.codejam2016;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.List;
 
 /**
  * Created by dont on 16/04/16.
  */
 public class TheLastWord {
     public static void main(String[] args) {
         try {
             List<String> inputs = Files.readAllLines(new File("X:\\A\\A-large.in").toPath());
             FileWriter fw = new FileWriter("X:\\A\\A-large.out");
 
             int T = Integer.valueOf(inputs.get(0));
             for (int i = 1; i <= T; i++) {
                 // handle each test case
                 String S = inputs.get(i);
                 StringBuilder lastWord = new StringBuilder();
                 for(char ch : S.toCharArray()) {
                     if(lastWord.length() == 0) {
                         lastWord.append(ch);
                     } else {
                         char firstChar = lastWord.toString().charAt(0);
                         if(ch >= firstChar) {
                             lastWord.insert(0, ch);
                         } else {
                             lastWord.append(ch);
                         }
                     }
                 }
                 fw.write("Case #" + i + ": " + lastWord.toString() + "\n");
                 fw.flush();
             }
             fw.flush();
             fw.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }

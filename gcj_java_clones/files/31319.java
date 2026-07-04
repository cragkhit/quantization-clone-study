package round1;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public class Test {
     public static void main(String[] args) throws NumberFormatException, IOException {
         // BufferedReader br = new BufferedReader(new
         // InputStreamReader(System.in));
         BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("text")));
         int testCount = Integer.parseInt(br.readLine());
 
         for (int i = 1; i <= testCount; i++) {
             String a = br.readLine();
             char[] cha = a.toCharArray();
 //            List<String> stri = new ArrayList<>();
 //            stri.add(String.valueOf(cha[0]));
             String answer  = ""+cha[0];
             for (int j = 1; j < cha.length; j++) {
                 if(answer.charAt(0)>cha[j])
                     answer = answer+cha[j];
                 else
                     answer = cha[j] + answer;
 
 
             }
 
             System.out.println("Case #" + i + ": " + answer);
         }
     }
 }

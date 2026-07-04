import java.util.*;
 import java.io.File;
 
 
 class GCJ161A {
 
     public static void main(String args[] ) throws Exception {
         GCJ161A sol = new GCJ161A();
         sol.solve();
     }
 
 
     void solve() throws Exception {
 
         //Scanner sc = new Scanner(System.in);
         //Scanner sc = new Scanner(new File("GCJ161A.sample.in"));
         Scanner sc = new Scanner(new File("GCJ161A-A-large.in"));
         
         int T = sc.nextInt();
         // System.out.println("T = " + T);
         sc.nextLine();
 
         for (int t = 0; t < T; t++) {
             String input = sc.nextLine();
 
             System.out.println("Case #" + (t+1) + ": " + calc(input));
         }
     }
 
     String calc(String input) {
         //System.out.println("intput = " + input);
 
         String output = "" + input.charAt(0);
 
         for (int i = 1; i < input.length(); i++) {
             char ch = input.charAt(i);
             if (ch >= output.charAt(0)) {
                 output = "" + ch + output;
             }
             else {
                 output = output + ch;
             }
         }
 
         return output;
 
         //return "";
 
     }
 
 }
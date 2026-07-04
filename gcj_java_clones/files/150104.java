import java.util.*;
 import java.io.*;
 import java.math.*;
 
 public class Fractiles {
     public static void main(String[] args) {
         
         Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
         int t = in.nextInt();    // Scanner has functions to read ints, longs, strings, chars, etc.
 
         for (int i = 1; i <= t; ++i) {
         
             int k = in.nextInt();
             int c = in.nextInt();
             int s = in.nextInt();
 
             BigInteger bigK = BigInteger.valueOf(k);
             BigInteger pos;
 
             if (s < k - 1) {
                 System.out.println("Case #" + i + ": IMPOSSIBLE");
             } 
             else if (c == 1) {
 
                 if (s < k) {
                     System.out.println("Case #" + i + ": IMPOSSIBLE");    
                 }
                 else {
                     System.out.print("Case #" + i + ": ");
                     for (int z = 1; z <= k; z++) {
                         System.out.print(" " + z);
                     }
                     System.out.println();
                 }
             }
             else {
 
                 String res[] = new String[k];
 
                 boolean didSomething = false;
 
                 for (int w = 0; w < k - 1; w++) {
                     pos = BigInteger.ZERO;
                     for (int j =1; j <= c; j++) {
                         pos = pos.add(bigK.pow(c - j));
                     }
                     pos = pos.multiply(BigInteger.valueOf(w));
                     res[w] = pos.add(BigInteger.ONE).add(BigInteger.ONE).toString();
                     didSomething = true;
                 }
 
                 if (!didSomething) {
                     System.out.println("Case #" + i + ": IMPOSSIBLE");   
                 }
                 else {
                     System.out.print("Case #" + i + ": ");
                     for (int w = 0; w < k - 1; w++) {
                         System.out.print(" " + res[w]);
                     }
                     System.out.println();
                 }
 
             }
         }
     }
 }
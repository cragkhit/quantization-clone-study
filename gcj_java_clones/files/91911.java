import java.util.*;
 
 public class A {
     static Scanner s;
     public static void main(String[] args) {
         s = new Scanner(System.in);
         int t = s.nextInt();
         for (int i = 0; i < t; i++) {
             solve(i+1);
         }
     }
 
     public static void solve(int x) {
         int smax = s.nextInt();
         String str = s.next();
         int ppl = 0;
         int count = 0;
         for (int i = 0; i <= smax; i++) {
             int k = str.charAt(i) - '0';
             if ( ppl >= i) {
                 ppl += k;
             } else {
                 count += (i - ppl);
                 ppl = i;
                 ppl += k;
             }
         }
         System.out.println("Case #" + x + ": " + count);
     }
 }

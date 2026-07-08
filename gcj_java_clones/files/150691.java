/*
 Keep solving problems. 
 */
 
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.*;
 
 public class GCJ2016B {
 
     Scanner             sc       = new Scanner(getClass().getResourceAsStream(IN));
 //    Scanner sc = new Scanner(System.in);
     static final String FILENAME = "B-large";
     static final String IN       = FILENAME + ".in";
     static final String OUT      = FILENAME + ".out";
     PrintStream         out      = System.out;
 
     private void solve() {
         char[] seq = sc.next().toCharArray();
         int res = 0;
         int n = seq.length;
 
         int[] arr = new int[n];
         for (int i = 0; i < n; i++) {
             arr[i] = seq[i] == '+' ? 1 : 0;
         }
 
         int i = n - 1;
 
         while (i >= 0) {
             if (arr[i] == 0) {
                 if (i == 0) {
                     res++;
                     break;
                 } else {
                     if (arr[0] == 1) {
                         int j = 0;
                         while (true) {
                             if (arr[j] == 1) {
                                 arr[j] = 0;
                                 j++;
                             } else {
                                 break;
                             }
                         }
                         res++;
                     } else {
                         swap(0, i, arr);
                         res++;
                     }
                 }
             } else {
                 i--;
             }
         }
 
         out.println(res);
     }
 
     private void swap(int start, int end, int[] arr) {
         for(int i = start, j = end; i <= j; i++, j--) {
             if(i < j) {
                 int tmp = arr[i];
                 arr[i] = 1 - arr[j];
                 arr[j] = 1 - tmp;
             }
             else {
                 arr[i] = 1 - arr[i];
             }
         }
     }
 
     private void run() throws Exception {
          out = new PrintStream(new FileOutputStream(OUT));
         int t = sc.nextInt();
         for (int i = 1; i <= t; i++) {
             out.print("Case #" + i + ": ");
             solve();
         }
         sc.close();
         out.close();
     }
 
     public static void main(String args[]) throws Exception {
         new GCJ2016B().run();
     }
 
 }
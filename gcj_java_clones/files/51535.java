import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 public class Fractiles {
 
     static long k, c, s, pow;
 
     private static long dfs(long low, long high, int offset, long stepLength) {
 //        System.out.println(low + " " + high + " " + offset + " " + stepLength);
         if (stepLength == 0) return low;
         long newLow = Math.min(low + offset * stepLength, high - 1);
         long newHigh = Math.min(high, newLow + stepLength);
         return dfs(newLow, newHigh, offset + 1, stepLength / k);
     }
 
     public static void main(String[] args) throws FileNotFoundException {
 //        Scanner cin = new Scanner(new File("D-small-attempt0.in"));
 //        PrintStream cout = new PrintStream("D-small-attempt0.out");
         Scanner cin = new Scanner(new File("D-large.in"));
         PrintStream cout = new PrintStream("D-large.out");
 //        Scanner cin = new Scanner(System.in);
 //        PrintStream cout = System.out;
 
         int _case = 0;
         for (int T = cin.nextInt(); T > 0; T--) {
             _case++;
             StringBuilder ans = new StringBuilder();
             k = cin.nextLong();
             c = cin.nextLong();
             s = cin.nextLong();
 
             // pow = k^(c-1)
             pow = 1;
             for (int i = 0; i < c - 1; i++) pow *= k;
             // Basically, ceil(k / c)
             if ((k + c - 1) / c > s) ans.append(" IMPOSSIBLE");
             else if (k == 1) ans.append(" 1");
             else {
                 for (int i = 0; i < k; i += c) {
                     ans.append(" ");
                     ans.append(dfs(0, pow * k, i, pow) + 1);
                 }
             }
 
             cout.printf("Case #%d:%s\n", _case, ans.toString());
         }
 
         cin.close();
         cout.close();
     }
 }

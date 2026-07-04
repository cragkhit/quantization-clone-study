package codejam;
 
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.Scanner;
 
 public class FairAndSquare {
 
     final int NUM = 100;
     static int num = 0;
     final static long[] F_SQ = new long[100];
 
     final static int[] digitsCache = new int[100];
     static int m;
     static int isOdd;
 
     static {
         calculateFSQ(9);
     }
 
     interface Function {
         void process(long n);
     }
 
     static void calculateFSQ(final int maxDigitsNum) {
         F_SQ[num++] = 0;
         Function f = new Function() {
             @Override
             public void process(final long n) {
                 long k = n * n;
                 if (isPalindrome(k)) {
 
 //                    System.out.println(n);
 //                    System.out.println(k);
                     F_SQ[num++] = k;
                 }
             }
         };
         for (int i = 1; i <= maxDigitsNum; ++i) {
             enumeratePalindromes(i, f);
         }
     }
 
     static void enumeratePalindromes(int digitsNum, Function f) {
         if (digitsNum == 1) {
             for (int i = 1; i < 10; ++i) {
                 f.process(i);
             }
             return;
         }
 
         Arrays.fill(digitsCache, 0);
         final int pos = (digitsNum + 1) / 2 - 1;
         if (digitsNum % 2 == 0) {//even number
             m = pos;
             isOdd = 0;
             enum_2(f, pos);
         } else {
             m = pos;
             isOdd = 1;
             for (int i = 0; i < 10; ++i) {
                 digitsCache[m] = i;
                 enum_2(f, m - 1);
             }
         }
     }
 
     static void enum_2(Function f, int pos) {
         if (pos > 0)
             for (int i = 0; i < 10; ++i) {
                 digitsCache[pos] = i;
                 enum_2(f, pos - 1);
             }
         else if (pos == 0) {
             for (int i = 1; i < 10; ++i) {
                 digitsCache[pos] = i;
                 enum_2(f, pos - 1);
             }
         } else {
             if (digitsCache[0] == 0)
                 return;
             long n = 0;
             long m10 = 1;
             for (int j = 0; j <= m; ++j) {
                 n += m10 * digitsCache[j];
                 m10 *= 10;
             }
             for (int j = m - isOdd; j >=0 ; --j) {
                 n += m10 * digitsCache[j];
                 m10 *= 10;
             }
             f.process(n);
         }
     }
 
     static boolean isPalindrome(long n) {
         Arrays.fill(digitsCache, 0);
         int i = 0;
         while (n != 0) {
             digitsCache[i++] = (int) (n % 10);
             n /= 10;
         }
         for (int j = 0; j < i / 2; ++j) {
             if (digitsCache[j] != digitsCache[i - j - 1])
                 return false;
         }
         return true;
     }
 
     long A;
     long B;
 
     void load(Scanner scan) {
         A = scan.nextLong();
         B = scan.nextLong();
     }
 
 
     void process() throws IOException {
         PrintWriter out = new PrintWriter(//System.out);
             new BufferedWriter(new FileWriter("large.txt")));
         BufferedReader in = new BufferedReader(new FileReader("C-large-1.in"));
         Scanner scan = new Scanner(in);
         final int num = scan.nextInt();
         for (int n = 1; n <= num; ++n) {
             load(scan);
             out.format("Case #%d: %s%n", n, getNum());
         }
 
         in.close();
         out.close();
     }
 
 
 
     int getNum(){
        final int nA = getNumLessThan(A);
        int nB = getNumLessThan(B + 1);
         return nB - nA;
     }
 
     int getNumLessThan(final long K) {
         int pos = Arrays.binarySearch(F_SQ, 0, num, K);
         final long nA;
         if (pos > 0) {
             return pos - 1;
         } else {
             if (pos == 0 || pos == -1)
                 throw new AssertionError();
             return - pos - 2;
         }
     }
 
     public static void main(final String[] args) throws IOException {
 //        calculateFSQ(15);
         new FairAndSquare().process();
     }
 
 
 }
package Qualifier;
 
 
 import java.io.*;
 import java.util.InputMismatchException;
 import java.util.LinkedList;
 
 
 public class Q3 {
 
     private final double max;
 
     public static void main(String[] args)
             throws FileNotFoundException {
         OutputStream outputStream = null;
         File file = null;
         String inFile = System.getProperty("inFile");
         try {
             if (inFile != null) {
                 final String outFilename = inFile + ".out";
                 file = new File(outFilename);
                 final FileOutputStream fos = new FileOutputStream(file);
                 outputStream = new TeeOutputStream(System.out, fos);
             } else {
                 outputStream = System.out;
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         final PrintStream printStream = new PrintStream(outputStream);
         System.setOut(printStream);
         InputStream is;
         if (inFile != null)
             is = new FileInputStream(inFile + ".in");
         else {
             is = System.in;
         }
         InputReader in = new StreamInputReader(is);
         PrintWriter out = new PrintWriter(System.out);
         run(in, out);
 
         if (inFile != null) {
             assert (file != null);
             out.println("wrote to:\n" + file.getAbsolutePath());
         }
     }
 
     public Q3() {
         results.add(new Result(0, 0));
         max = Math.pow(10, 14);
         preSolveForRange(1, (long) max);
 
         //System.out.println((long) Math.pow(10, 14));
     }
 
     public static void run(InputReader in, PrintWriter out) {
         Q3 solver = new Q3();
         int testCount = in.readInt();
         for (int i = 1; i <= testCount; i++) {
             solver.solve(i, in, out);
             out.flush();
         }
         exit(in, out);
     }
 
     public static void exit(InputReader in, PrintWriter out) {
         in.close();
         out.close();
     }
 
 
     public void solve(final int caseNumber, InputReader in, PrintWriter out) {
         final long A = Long.parseLong(in.readWord());
         final long B = Long.parseLong(in.readWord());
         final long startT = System.nanoTime();
         String ans = Long.toString(solveForRange(A, B));
         final long endT = System.nanoTime();
         //ans += "\t took: " + (endT - startT);
 
         //ans = possible ? "YES" : "NO";
 
         //print
         out.format("Case #%d: %s\n", new Object[]{Integer.valueOf(caseNumber), ans});
     }
 
     private long solveForRange(long a, long b) {
         long Art = (long) Math.ceil(Math.sqrt(a));
         long Brt = (long) Math.floor(Math.sqrt(b));
         if (b>max)
             throw new RuntimeException("B too high");
         /*final Long bLong = results.get(Brt);
         final Long aLong = results.get(Art);*/
         Result high = null;
         Result low = null;
         for (Result r : results) {
             if (r.n < Art)
                 low = r;
 
 
             if (r.n > Brt)
                 break;
             else
                 high = r;
         }
         return high.numToHere - low.numToHere;
     }
 
     private long preSolveForRange(long a, long b) {
         long ans = 0;
         long Art = (long) Math.ceil(Math.sqrt(a));
         long Brt = (long) Math.floor(Math.sqrt(b));
 
         for (long n = Art; n <= Brt; n++) {
             //if (isPalindrome(n) && isPalindrome(n * n))
             final long nn = n * n;
             if (isPalindromeStr4(Long.toString(n))
                     && isPalindromeStr4(Long.toString(nn))) {
                 ans++;
                 saveResult(n, nn, ans);
             }
         }
         return ans;
     }
 
     //how many from this including this
     static class Result {
         final long n;
         final long numToHere;
 
         Result(long n, long numToHere) {
             this.n = n;
             this.numToHere = numToHere;
         }
     }
 
     LinkedList<Result> results = new LinkedList<>();
     /*(new Comparator<Long>() {
         public int compare(Long o1, Long o2) {
             final long res = o1 - o2;
             if (res < 0)
                 return -1;
             if (res > 0)
                 return 1;
             return 0;
         }
     }*/
 
 
     private void saveResult(long n, long l, long ZeroToThis) {
         results.add(new Result(n, ZeroToThis));
     }
 
     public static boolean isPalindrome(long number) {
         long palindrome = number; // copied number into variable
         long reverse = 0;
 
         while (palindrome != 0) {
             long remainder = palindrome % 10;
             reverse = reverse * 10 + remainder;
             palindrome = palindrome / 10;
         }
 
         // if original and reverse of number is equal means
         // number is palindrome in Java
         if (number == reverse) {
             return true;
         }
         return false;
     }
 
     public static boolean isPalindromeStr1(byte[] wort) {
         boolean palindrom = false;
         if (wort.length % 2 == 0) {
             for (int i = 0; i < wort.length / 2 - 1; i++) {
                 if (wort[i] != wort[wort.length - i - 1]) {
                     return false;
                 } else {
                     palindrom = true;
                 }
             }
         } else {
             for (int i = 0; i < (wort.length - 1) / 2 - 1; i++) {
                 if (wort[i] != wort[wort.length - i - 1]) {
                     return false;
                 } else {
                     palindrom = true;
                 }
             }
         }
         return palindrom;
     }
 
     public static boolean isPalindromeStr2(byte[] wort) {
         if (wort.length % 2 == 0) {
             for (int i = 0; i < wort.length / 2 - 1; i++) {
                 if (wort[i] != wort[wort.length - i - 1]) {
                     return false;
                 }
             }
         } else {
             for (int i = 0; i < (wort.length - 1) / 2 - 1; i++) {
                 if (wort[i] != wort[wort.length - i - 1]) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public static boolean isPalindromeStr3(byte[] word) {
         int i1 = 0;
         int i2 = word.length - 1;
         while (i2 > i1) {
             if (word[i1] != word[i2]) {
                 return false;
             }
             ++i1;
             --i2;
         }
         return true;
     }
 
     public static boolean isPalindromeStr4(String str) {
         return str.equals(new StringBuffer().append(str).reverse().toString());
     }
 
 
     static class Pair<U, V>
             implements Comparable<Pair<U, V>> {
         public final U first;
         public final V second;
 
         public static <U, V> Pair<U, V> makePair(U first, V second) {
             return new Pair(first, second);
         }
 
         private Pair(U first, V second) {
             this.first = first;
             this.second = second;
         }
 
         public boolean equals(Object o) {
             if (this == o) return true;
             if ((o == null) || (getClass() != o.getClass())) return false;
 
             Pair pair = (Pair) o;
 
             if (this.first != null ? this.first.equals(pair.first) : pair.first == null) ;
             return this.second != null ? this.second.equals(pair.second) : pair.second == null;
         }
 
         public int hashCode() {
             int result = this.first != null ? this.first.hashCode() : 0;
             result = 31 * result + (this.second != null ? this.second.hashCode() : 0);
             return result;
         }
 
         public String toString() {
             return "(" + this.first + "," + this.second + ")";
         }
 
         public int compareTo(Pair<U, V> o) {
             int value = ((Comparable) this.first).compareTo(o.first);
             if (value != 0)
                 return value;
             return ((Comparable) this.second).compareTo(o.second);
         }
     }
 
     static class StreamInputReader extends InputReader {
         private InputStream stream;
         private byte[] buf = new byte[1024];
         private int curChar;
         private int numChars;
 
         public StreamInputReader(InputStream stream) {
             this.stream = stream;
         }
 
         public int read() {
             if (this.numChars == -1)
                 throw new InputMismatchException();
             if (this.curChar >= this.numChars) {
                 this.curChar = 0;
                 try {
                     this.numChars = this.stream.read(this.buf);
                 } catch (IOException e) {
                     throw new InputMismatchException();
                 }
                 if (this.numChars <= 0)
                     return -1;
             }
             return this.buf[(this.curChar++)];
         }
 
         public void close() {
             try {
                 this.stream.close();
             } catch (IOException ignored) {
             }
         }
     }
 
     static abstract class InputReader {
         public abstract int read();
 
         public int readInt() {
             int c = read();
             while (isSpaceChar(c))
                 c = read();
             int sgn = 1;
             if (c == 45) {
                 sgn = -1;
                 c = read();
             }
             int res = 0;
             do {
                 if ((c < 48) || (c > 57))
                     throw new InputMismatchException();
                 res *= 10;
                 res += c - 48;
                 c = read();
             } while (!isSpaceChar(c));
             return res * sgn;
         }
 
         public String readWord() {
             int c = read();
             while (isSpaceChar(c))
                 c = read();
             StringBuilder res = new StringBuilder();
             do {
                 res.appendCodePoint(c);
                 c = read();
             } while (!isSpaceChar(c));
             return res.toString();
         }
 
         public String readNextLine() {
             int c = read();
             while (isSpaceChar(c))
                 c = read();
             StringBuilder res = new StringBuilder();
             do {
                 res.appendCodePoint(c);
                 c = read();
             } while (isNewLine(c));
             return res.toString();
         }
 
         private boolean isNewLine(int c) {
             return (c != 10) && (c != 13);
         }
 
         private boolean isSpaceChar(int c) {
             return (c == 32) || (c == 10) || (c == 13) || (c == 9) || (c == -1);
         }
 
         public char readCharacter() {
             int c = read();
             while (isSpaceChar(c))
                 c = read();
             return (char) c;
         }
 
         public abstract void close();
     }
 
     public static final class TeeOutputStream extends OutputStream {
 
         private final OutputStream out;
         private final OutputStream tee;
 
         public TeeOutputStream(OutputStream out, OutputStream tee) {
             if (out == null)
                 throw new NullPointerException();
             else if (tee == null)
                 throw new NullPointerException();
 
             this.out = out;
             this.tee = tee;
         }
 
         @Override
         public void write(int b) throws IOException {
             out.write(b);
             tee.write(b);
         }
 
         @Override
         public void write(byte[] b) throws IOException {
             out.write(b);
             tee.write(b);
         }
 
         @Override
         public void write(byte[] b, int off, int len) throws IOException {
             out.write(b, off, len);
             tee.write(b, off, len);
         }
 
         @Override
         public void flush() throws IOException {
             out.flush();
             tee.flush();
         }
 
         @Override
         public void close() throws IOException {
             out.close();
             tee.close();
         }
     }
 }
 

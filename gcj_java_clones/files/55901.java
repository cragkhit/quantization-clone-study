import java.io.*;
 import java.util.StringTokenizer;
 
 public class Main {
 
     static MyScanner   in;
     static PrintWriter out;
 //    static Timer timer = new Timer();
 
     public static void main(String[] args) throws IOException {
         in = new MyScanner("A-large.in");
 //        out = new PrintWriter(new BufferedOutputStream(System.out), true);
         out = new PrintWriter(new BufferedWriter(new FileWriter("solve.txt")));
 
         int t = in.nextInt();
 
         for (int i = 0; i < t; i++) {
             long ans = solve(in.nextInt());
             out.println("Case #" + (i + 1) + ": " + (ans == 0 ? "INSOMNIA" : ans));
         }
 
         out.close();
     }
 
     static long solve(long n) {
         if (n == 0)
             return 0;
 
         boolean[] arr = new boolean[10];
         int left = 10;
         int mn = 1;
 
         while (left > 0) {
             long a = n * mn;
             while (a != 0) {
                 int last = (int) (a % 10);
                 if (!arr[last]) {
                     arr[last] = true;
                     left--;
                 }
                 a /= 10;
             }
 
             mn++;
         }
 
         return n * (mn - 1);
     }
 
 }
 
 class MyScanner {
 
     private final BufferedReader  br;
     private       StringTokenizer st;
     private       String          last;
 
     public MyScanner() {
         br = new BufferedReader(new InputStreamReader(System.in));
     }
 
     public MyScanner(String path) throws IOException {
         br = new BufferedReader(new FileReader(path));
     }
 
     public MyScanner(String path, String decoder) throws IOException {
         br = new BufferedReader(new InputStreamReader(new FileInputStream(path), decoder));
     }
 
     String next() throws IOException {
         while (st == null || !st.hasMoreElements())
             st = new StringTokenizer(br.readLine());
         last = null;
         return st.nextToken();
     }
 
     String nextLine() throws IOException {
         st = null;
         return (last == null) ? br.readLine() : last;
     }
 
     boolean hasNext() {
         if (st != null && st.hasMoreElements())
             return true;
 
         try {
             while (st == null || !st.hasMoreElements()) {
                 last = br.readLine();
                 st = new StringTokenizer(last);
             }
         }
         catch (Exception e) {
             return false;
         }
 
         return true;
     }
 
     String[] nextStrings(int n) throws IOException {
         String[] arr = new String[n];
         for (int i = 0; i < n; i++)
             arr[i] = next();
         return arr;
     }
 
     String[] nextLines(int n) throws IOException {
         String[] arr = new String[n];
         for (int i = 0; i < n; i++)
             arr[i] = nextLine();
         return arr;
     }
 
     int nextInt() throws IOException {
         return Integer.parseInt(next());
     }
 
     int[] nextInts(int n) throws IOException {
         int[] arr = new int[n];
         for (int i = 0; i < n; i++)
             arr[i] = nextInt();
         return arr;
     }
 
     Integer[] nextIntegers(int n) throws IOException {
         Integer[] arr = new Integer[n];
         for (int i = 0; i < n; i++)
             arr[i] = nextInt();
         return arr;
     }
 
     int[][] next2Ints(int n, int m) throws IOException {
         int[][] arr = new int[n][m];
         for (int i = 0; i < n; i++)
             for (int j = 0; j < m; j++)
                 arr[i][j] = nextInt();
         return arr;
     }
 
     long nextLong() throws IOException {
         return Long.parseLong(next());
     }
 
     long[] nextLongs(int n) throws IOException {
         long[] arr = new long[n];
         for (int i = 0; i < n; i++)
             arr[i] = nextLong();
         return arr;
     }
 
     long[][] next2Longs(int n, int m) throws IOException {
         long[][] arr = new long[n][m];
         for (int i = 0; i < n; i++)
             for (int j = 0; j < m; j++)
                 arr[i][j] = nextLong();
         return arr;
     }
 
     double nextDouble() throws IOException {
         return Double.parseDouble(next().replace(',', '.'));
     }
 
     double[] nextDoubles(int size) throws IOException {
         double[] arr = new double[size];
         for (int i = 0; i < size; i++)
             arr[i] = nextDouble();
         return arr;
     }
 
     boolean nextBool() throws IOException {
         String s = next();
         if (s.equalsIgnoreCase("true") || s.equals("1"))
             return true;
 
         if (s.equalsIgnoreCase("false") || s.equals("0"))
             return false;
 
         throw new IOException("Boolean expected, String found!");
     }
 }
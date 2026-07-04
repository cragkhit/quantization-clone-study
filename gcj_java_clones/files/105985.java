package gcj;
 
 import java.util.*;
 import java.io.*;
 
 public class B implements Runnable {
        
 	public void solve() throws IOException {
             int T = nextInt();
 			for(int t = 1; t <= T; t++){
 				printCase(t);
 				solve1();
 				System.out.println("Completed: " + t + "/" + T);
 			}
 	}
 	
 	public void solve1() throws IOException{
 		int N = nextInt();
                 int[] b = new int[N];
                 for(int i = 0; i < N; i++) b[i] = nextInt();
                 
                 int answer = Integer.MAX_VALUE;
                 
                 for(int a = 1; a <= 1000; a++){
                         int sleep = 0;
                         for(int i = 0; i < N; i++){
                                 if(b[i] > a){
                                         sleep += (b[i] - 1) / a;
                                 }
                         }
                         
                         answer = Math.min(answer, sleep + a);
                 }
                 
                 out.println(answer);
 	}
        
         
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	//-----------------------------------------------------------
 	public static void main(String[] args) {
 		new B().run();
 	}
 
         public void printCase(int t){
             out.print("Case #"+ t + ": ");
         }
 
         public void printCaseLn(int t){
             out.println("Case #"+ t + ": ");
         }
 
         public void debug(Object... arr){
             System.out.println(Arrays.deepToString(arr));
         }
 
         public void print1Int(int[] a){
                 for(int i = 0; i < a.length; i++)
                         System.out.print(a[i] + " ");
                 System.out.println();
         }
         
         public void print2Int(int[][] a){
                 for(int i = 0; i < a.length; i++){
                         for(int j = 0; j < a[0].length; j++){
                                 System.out.print(a[i][j] + " ");
                         }
                         System.out.println();
                 }
         }
         
 	public void run() {
 		try {
 			in = new BufferedReader(new FileReader("B.in"));
                         out = new PrintWriter(new File("B.out"));
 			tok = null;
 			solve();
 			in.close();
                         out.close();
 		} catch (IOException e) {
 			System.exit(0);
 		}
 	}
 
 	public String nextToken() throws IOException {
 		while (tok == null || !tok.hasMoreTokens()) {
 			tok = new StringTokenizer(in.readLine());
 		}
 		return tok.nextToken();
 	}
 
 	public int nextInt() throws IOException {
 		return Integer.parseInt(nextToken());
 	}
 
 	public long nextLong() throws IOException {
 		return Long.parseLong(nextToken());
 	}
 
 	public double nextDouble() throws IOException {
 		return Double.parseDouble(nextToken());
 	}
         PrintWriter out;
 	BufferedReader in;
 	StringTokenizer tok;
 }
 
 
 
 

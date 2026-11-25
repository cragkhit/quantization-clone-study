package jam13;
 
 import java.io.*;
 import java.util.*;
 
 public class QualA{		
 		
 		private BufferedReader in;	
 		private StringTokenizer st;
 		private PrintWriter out;
 		
 		HashSet<String> h;
 		
 		boolean xWon(){
 			String []x = {"XXXX","XXXT","XXTX","XTXX","TXXX"};
 			for(String a : x){
 				if(h.contains(a)) return true;
 			}
 			return false;
 		}
 		
 		boolean oWon(){
 			String []o = {"OOOO","OOOT","OOTO","OTOO","TOOO"};
 			for(String a : o){
 				if(h.contains(a)) return true;
 			}
 			return false;
 		}
 		void solve() throws IOException{
 			
 			int kases = nextInt();
 			int kase = 0;
 			while(kases-->0){
 				kase++;
 				out.print("Case #"+kase+": ");
 				boolean dot = false;
 				String a,b,c,d;
 				a = next();
 				b = next();
 				c = next();
 				d = next();
 				dot = a.contains(".") || b.contains(".") || c.contains(".") || d.contains(".");
 				h = new HashSet<String>();				
 				h.add(a);h.add(b);
 				h.add(c);h.add(d);
 				h.add(a.charAt(0)+""+b.charAt(0)+""+c.charAt(0)+""+d.charAt(0));
 				h.add(a.charAt(1)+""+b.charAt(1)+""+c.charAt(1)+""+d.charAt(1));
 				h.add(a.charAt(2)+""+b.charAt(2)+""+c.charAt(2)+""+d.charAt(2));
 				h.add(a.charAt(3)+""+b.charAt(3)+""+c.charAt(3)+""+d.charAt(3));
 				
 				h.add(a.charAt(0)+""+b.charAt(1)+""+c.charAt(2)+""+d.charAt(3));
 				h.add(a.charAt(3)+""+b.charAt(2)+""+c.charAt(1)+""+d.charAt(0));
 				
 				
 				if(xWon()){
 					out.println("X won");
 				}
 				else if(oWon()){
 					out.println("O won");
 				}
 				else if(dot){
 					out.println("Game has not completed");
 				}
 				else{
 					out.println("Draw");
 				}
 				in.readLine();
 				
 				
 			}
 								
 			
 		}
 			
 
 QualA() throws IOException {
 			in = new BufferedReader(new FileReader("input.txt"));	
 			out = new PrintWriter(new FileWriter("output.txt"));
 			eat("");
 			solve();	
 			out.close();
 		}
 
 		private void eat(String str) {
 			st = new StringTokenizer(str);
 		}
 
 		String next() throws IOException {
 			while (!st.hasMoreTokens()) {
 				String line = in.readLine();
 				if (line == null) {
 					return null;
 				}
 				eat(line);
 			}
 			return st.nextToken();
 		}
 
 		int nextInt() throws IOException {
 			return Integer.parseInt(next());
 		}
 
 		long nextLong() throws IOException {
 			return Long.parseLong(next());
 		}
 
 		double nextDouble() throws IOException {
 			return Double.parseDouble(next());
 		}
 
 		public static void main(String[] args) throws IOException {
 			new QualA();
 		}
 
 		int gcd(int a,int b){
 			if(b>a) return gcd(b,a);
 			if(b==0) return a;
 			return gcd(b,a%b);
 		}
 
 }

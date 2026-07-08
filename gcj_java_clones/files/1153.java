import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 public class A_TheLastWord {
 
 	public static void main(String[] args) {
 		Reader.init();
 		int T = Reader.nextInt();
 		
 		for(int main=0; main<T; main++) {
 			char[] S = Reader.next().toCharArray();
 			StringBuilder builder = new StringBuilder();
 			
 			for(char c : S) {
 				if(builder.length() == 0) {
 					builder.append(c);
 					continue;
 				}
 				
 				if(c >= builder.charAt(0)) builder.insert(0, c);
 				else builder.append(c);
 			}
 			
 			System.out.println("Case #" + (main+1) + ": " + builder.toString());
 		}
 	}
 	
 	static class Reader {
 		private static BufferedReader in;
 		private static StringTokenizer tokenizer;
 		
 		public static void init() {
 			in = new BufferedReader(new InputStreamReader(System.in));
 			tokenizer = new StringTokenizer("");
 		}
 		
 		public static String next() {
 			while(!tokenizer.hasMoreTokens())
 				try {
 					tokenizer = new StringTokenizer(in.readLine());
 				} catch (IOException e) {
 				}
 			return tokenizer.nextToken();
 		}
 		
 		public static int nextInt() {
 			return Integer.parseInt(next());
 		}
 		
 		public static long nextLong() {
 			return Long.parseLong(next());
 		}
 	}
 }

import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.HashSet;
 import java.util.StringTokenizer;
 
 public class Solution {
 
 	static InputReader in = new InputReader(System.in);
 	static HashSet<Character> hs = new HashSet<>();
 
 	public static void main(String[] args) throws FileNotFoundException,
 			UnsupportedEncodingException {
 
 		PrintWriter printWriter = new PrintWriter(
 				"/home/omar/Desktop/file.txt", "UTF-8");
 
 		int T = in.nextInt();
 
 		for (int i = 1; i <= T; i++) {
 			long input = in.nextLong();
 
 			update(input + "");
 
 			long incrementor = input;
 
 			while (hs.size() != 10 && input != 0) {
 				incrementor += input;
 				update(incrementor + "");
 			}
 
 			hs.clear();
 			printWriter.write("Case #" + i + ": ");
 			printWriter.write(input == 0 ? "INSOMNIA\n" : incrementor + "\n");
 			
 		}
 
 		printWriter.close();
 		
 	}
 
 	public static void update(String input) {
 		for (int i = 0; i < input.length(); i++) {
 			hs.add(input.charAt(i));
 		}
 	}
 
 }
 
 class InputReader {
 	private BufferedReader reader;
 	private StringTokenizer tokenizer;
 
 	public InputReader(InputStream stream) {
 		reader = new BufferedReader(new InputStreamReader(stream), 32768);
 		tokenizer = null;
 	}
 
 	public String next() {
 		while (tokenizer == null || !tokenizer.hasMoreTokens()) {
 			try {
 				tokenizer = new StringTokenizer(reader.readLine());
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return tokenizer.nextToken();
 	}
 
 	public String readLine() {
 		try {
 			return reader.readLine();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public int nextInt() {
 		return Integer.parseInt(next());
 	}
 
 	public long nextLong() {
 		return Long.parseLong(next());
 	}
 
 	public double nextDouble() {
 		return Double.parseDouble(next());
 	}
 
 	public float nextFloat() {
 		return Float.parseFloat(next());
 	}
 
 }
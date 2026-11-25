import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.PriorityQueue;
 
 public class Prob1
 {
 	private static final int PROB = 1;
 	private static final int ATTEMPT = 2;
 
 	public static void main(final String[] args) throws IOException
 	{
 		BufferedReader reader = new BufferedReader(new FileReader(
 				String.format("data%d%d.txt", PROB, ATTEMPT)));
 		PrintWriter writer = new PrintWriter(new FileWriter(String.format(
 				"out%d%d.txt", PROB, ATTEMPT)));
 		int noCases = Integer.parseInt(reader.readLine());
 		for (int testCase = 1; testCase <= noCases; testCase++) {
 			int n = Integer.parseInt(reader.readLine());
 			Input[] lines = new Input[n];
 			boolean possible = true;
 			lines[0] = new Input(reader.readLine());
 			for (int i = 1; i < n; i++) {
 				lines[i] = new Input(reader.readLine());
 				if (!lines[i].equals(lines[0]))
 					possible = false;
 			}
 			if (!possible) {
 				writer.printf("Case #%d: Fegla Won%n", testCase);
 			} else {
 				int[] count = new int[lines[0].charCount.length];
 				for (int i = 0; i < n; i++) {
 					for (int p = 0; p < count.length; p++) {
 						count[p] += lines[i].charCount[p];
 					}
 				}
 				for (int p = 0; p < count.length; p++) {
 					count[p] /= n;
 				}
 				int total = 0;
 				for (int i = 0; i < n; i++) {
 					for (int p = 0; p < count.length; p++) {
 						total += Math.abs(count[p] - lines[i].charCount[p]);
 					}
 				}
 				writer.printf("Case #%d: %d%n", testCase, total);
 			}
 		}
 		writer.close();
 		reader.close();
 	}
 
 	static class Input
 	{
 		String current;
 		String minimum;
 		int[] charCount;
 
 		Input(String line)
 		{
 			current = line;
 			StringBuilder strb = new StringBuilder();
 			strb.append(line.charAt(0));
 			int pos = 0;
 			for (int i = 1; i < line.length(); i++) {
 				if (strb.charAt(pos) != line.charAt(i)){
 					strb.append(line.charAt(i));
 					pos++;
 				}
 			}
 			minimum = strb.toString();
 			charCount = new int[minimum.length()];
 			charCount[0] = 1;
 			pos = 0;
 			for (int i = 1; i < line.length(); i++) {
 				if (minimum.charAt(pos) == line.charAt(i))
 					charCount[pos]++;
 				else {
 					pos++;
 					charCount[pos]++;
 				}
 			}
 		}
 
 		public boolean equals(Object other)
 		{
 			if (other instanceof Input) {
 				if (((Input) other).minimum.equals(minimum)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		public String toString()
 		{
 			return minimum;
 		}
 	}
 }

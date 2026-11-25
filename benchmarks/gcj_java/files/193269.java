import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 public class GettingDigits {
 	
 	public static void main(String[] args) throws IOException {
 		Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 		try (PrintStream stream = new PrintStream("C:/Users/krio/workspace/codejam-round1b/getting-digits.txt")) {
 			int t = in.nextInt();
 		    for (int i = 1; i <= t; ++i) {
 		        String word = in.next();
 		        printResult(i, solve(word), stream);
 		    }					
 		}
 	}
 
 	private static String solve(String word) {
 		HashMap<Character, Integer> map = new HashMap<>();
 		for (int i = 0; i < word.length(); i++) {
 			Character c = word.charAt(i);
 			if (map.containsKey(c)) {
 				map.put(c, map.get(c) + 1);
 			} else {
 				map.put(c, 1);
 			}
 		}		
 
 		List<Integer> results = new ArrayList<Integer>();
 		while (hasChar(map, 'Z')) {
 			removeChars(map, 'Z', 'E', 'R', 'O');
 			results.add(0);
 		}
 		
 		while (hasChar(map, 'W')) {
 			removeChars(map, 'T', 'W', 'O');
 			results.add(2);
 		}
 		
 		while (hasChar(map, 'U')) {
 			removeChars(map, 'F', 'O', 'U', 'R');
 			results.add(4);
 		}
 
 		while (hasChar(map, 'F')) {
 			removeChars(map, 'F', 'I', 'V', 'E');
 			results.add(5);
 		}		
 
 		while (hasChar(map, 'X')) {
 			removeChars(map, 'S', 'I', 'X');
 			results.add(6);
 		}		
 
 		while (hasChar(map, 'S')) {
 			removeChars(map, 'S', 'E', 'V', 'E', 'N');
 			results.add(7);
 		}		
 
 		while (hasChar(map, 'G')) {
 			removeChars(map, 'E', 'I', 'G', 'H', 'T');
 			results.add(8);
 		}		
 		
 		while (hasChar(map, 'I')) {
 			removeChars(map, 'N', 'I', 'N', 'E');
 			results.add(9);
 		}				
 		
 		while (hasChar(map, 'O')) {
 			removeChars(map, 'O', 'N', 'E');
 			results.add(1);
 		}		
 		
 		while (hasChar(map, 'T')) {
 			removeChars(map, 'T', 'H', 'R', 'E', 'E');
 			results.add(3);
 		}				
 
 		Collections.sort(results);
 		
 		StringBuilder sb = new StringBuilder();
 		for (Integer i : results) {
 			sb.append(i);
 		}
 		return sb.toString();
 	}
 		
 	private static boolean hasChar(Map<Character, Integer> map, Character c) {
 		return map.containsKey(c) && map.get(c) > 0;
 	}
 	
 	private static void removeChars(Map<Character, Integer> map, Character... array) {
 		for (Character c : array) {
 			map.put(c, map.get(c) - 1);			
 		}
 	}
 	
 	private static void printResult(int i, String result, PrintStream out) {
 		out.println("Case #" + i + ": " + result);		
 	}
 }

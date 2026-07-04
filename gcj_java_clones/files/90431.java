import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.management.RuntimeErrorException;
 
 
 public class ProblemD {
 
 	private static final String GABRIEL = "GABRIEL";
 	private static final String RICHARD = "RICHARD";
 
 	public static void main(String[] args) throws Exception {
 		Scanner scanner = new Scanner(new File("problemD-large.in"));
 		int testCases = scanner.nextInt();
 		List<String> solutions = new ArrayList<String>();		
 		
 		for (int i = 1; i <= testCases; i++) {
 			int size = scanner.nextInt();
 			int x = scanner.nextInt();
 			int y = scanner.nextInt();
 			
 			solutions.add(solve(size, x, y));
 		}
 		scanner.close();
 		
 		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File("problemD")));
 		int i = 1;
 		for (String name: solutions) {
 			fileWriter.write("Case #" + i + ": " + name);
 			if (i < solutions.size()) {
 				fileWriter.newLine();
 			}
 			i++;
 		}
 		fileWriter.close();
 	}
 
 	private static String solve(int size, int x, int y) {
 		if (size >= 7) {
 			return RICHARD;
 		}
 		if (size == 1) {
 			return GABRIEL;
 		}
 		if (size == 2) {
 			if (x == 1 && y == 1) {
 				return RICHARD;
 			}
 			if (x * y % 2 == 0) {
 				return GABRIEL;
 			}	
 			return RICHARD;
 		}
 		if (size == 3) {
 			if (x < 3 && y < 3) {
 				return RICHARD;
 			}
 			if (x < 2 || y < 2) {
 				return RICHARD;
 			}
 			if (x * y % 3 == 0) {
 				return GABRIEL;
 			}
 			return RICHARD;
 		}
 		if (size == 4) {
 			if (x < 4 && y < 4) {
 				return RICHARD;
 			}
 			if (x < 3 || y < 3) {
 				return RICHARD;
 			}
 			if (x * y % 4 == 0) {
 				return GABRIEL;
 			}
 			return RICHARD;
 		}
 		if (size == 5) {
 			if (x < 5 && y < 5) {
 				return RICHARD;
 			}
 			if (x < 3 || y < 3) {
 				return RICHARD;
 			}
 			if (x * y % 5 == 0) {
 				return GABRIEL;
 			}
 			return RICHARD;
 		}
 		if (size == 6) {
 			if (x < 6 && y < 6) {
 				return RICHARD;
 			}
 			if (x < 4 || y < 4) {
 				return RICHARD;
 			}
 			if (x * y % 5 == 0) {
 				return GABRIEL;
 			}
 			return RICHARD;
 		}
 		throw new RuntimeErrorException(new Error(), "no match");
 	}
 }

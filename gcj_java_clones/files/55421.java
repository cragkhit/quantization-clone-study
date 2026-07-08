package hu.hke.gjc2013.qr;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Date;
 import java.util.Scanner;
 
 public class Lawnmower {
 	public static void main(String[] args) {
 //		readAndSolve("resource/gcj2013/qr/lawnmower/sample.in", "resource/gcj2013/qr/lawnmower/sample.out");
 //		readAndSolve("resource/gcj2013/qr/lawnmower/B-small-attempt0.in", "resource/gcj2013/qr/lawnmower/B-small-attempt0.out");
 		readAndSolve("resource/gcj2013/qr/lawnmower/B-large.in", "resource/gcj2013/qr/lawnmower/B-large.out");
 //		readAndSolve(args[0], args[1]);
 	}
 	private static void readAndSolve(String inputName, String outputName) {
 		BufferedReader input = null;
 		BufferedWriter output = null;
 		Date d1 = new Date();
 		try {
 			input = new BufferedReader(new FileReader(inputName));
 			output = new BufferedWriter(new FileWriter(outputName));
 			String line1 = null;
 			String[] line2 = null;
 			int expectedCases = 0;
 			int actualCase = -1;
 			int rows = 0;
 			int cols = 0;
 			Scanner sc = null;
 			line1=input.readLine();
 			expectedCases = Integer.parseInt(line1);
 			for (actualCase = 1; actualCase<=expectedCases; actualCase++) {
 				line1 = input.readLine();
 				sc = new Scanner(line1);
 				rows = sc.nextInt();
 				cols = sc.nextInt();
 				line2 = new String[rows];
 				for (int i = 0; i< rows; i++) {
 					line2[i] = input.readLine();					
 				}
 				String result = solve(rows, cols, line2);
 				output.write("Case #" + actualCase +": " + result + "\n");				
 			}	
 			input.close();
 			output.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			
 		}
 	}
 	private static String solve(int rows, int cols, String[] line2) {
 		if (rows == 1 || cols == 1) {
 			return "YES";
 		}
 		int [][] gardenPattern = new int[rows][];
 		Scanner sc = null;
 		for (int i = 0; i < gardenPattern.length; i++) {
 			gardenPattern[i] = new int[cols];
 			sc = new Scanner(line2[i]);
 			for (int j = 0; j< cols ; j++) {
 				gardenPattern[i][j] = sc.nextInt();
 			}
 		}
 		if (findBad(gardenPattern)) {
 			return "NO";
 		}
 		return "YES";
 	}
 	private static boolean findBad(int[][] gardenPattern) {
 		int bigger = 0;
 		for (int i = 0; i < gardenPattern.length; i++) {
 			for (int j = 0; j < gardenPattern[i].length; j++) {
 				bigger = biggerInRow(i,j,gardenPattern)
 						+ biggerInCol(i,j,gardenPattern); 
 				if (bigger >= 2) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	private static int biggerInCol(int i, int j, int[][] gardenPattern) {
 		for (int k = 0; k < gardenPattern.length; k++) {
 			if (gardenPattern[i][j]< gardenPattern[k][j]) {
 				return 1;
 			}
 		}
 		return 0;
 	}
 	private static int biggerInRow(int i, int j, int[][] gardenPattern) {
 		for (int k = 0; k < gardenPattern[i].length; k++) {
 			if (gardenPattern[i][j]< gardenPattern[i][k]) {
 				return 1;
 			}
 		}
 		return 0;
 	}
 }

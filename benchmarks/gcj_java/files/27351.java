package com.figuraj.codejam;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Locale;
 import java.util.Scanner;
 
 public class QualificationB {
 
 	private static boolean DEBUG = true;
 	private static String FILE_IN = "D:/CodeJam/B-large.in";
 	private static String FILE_OUT = "D:/CodeJam/out/B.txt";
 
 	private static double solve(double c, double f, double x) {
 		double time = 0;
 		double p = 2;
 		while (true) {
 			double notBuy = time + x / p;
 			double toBuyFarm = c / p;
 			double buy = time + toBuyFarm + x / (p + f);
 			if (notBuy < buy) {
 				return notBuy;
 			}
 			time += c / p;
 			p += f;
 		}
 	}
 
 	private static void output(double result, int testCase) {
 		String s = "Case #" + testCase + ": " + result;
 		System.out.println(s);
 	}
 
 	public static void main(String[] args) throws FileNotFoundException {
 		if (DEBUG) {
 			System.setIn(new FileInputStream(FILE_IN));
 			System.setOut(new PrintStream(FILE_OUT));
 		}
 
 		Scanner sc = new Scanner(System.in);
 		sc.useLocale(Locale.US);
 		int T = sc.nextInt();
 		for (int i = 1; i <= T; i++) {
 			double c = sc.nextDouble();
 			double f = sc.nextDouble();
 			double x = sc.nextDouble();
 			output(solve(c, f, x), i);
 		}
 	}
 }

package tochi.gcj2014;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 public class A {
 	public static void main(String[] args) throws Exception {
 		System.setOut(new PrintStream(new File("output.txt")));
 		Scanner sc = new Scanner(new File("A-large.in"));
 		int T = Integer.parseInt(sc.nextLine());
 		for (int i = 1; i <= T; i++) {
 			String[] ss = sc.nextLine().split("/");
 			long P = Long.parseLong(ss[0]);
 			long Q = Long.parseLong(ss[1]);
 			String format = String.format("Case #%d: %s", i, answer(P, Q));
 			System.out.println(format);
 		}
 	}
 
 	private static String answer(long p, long q) {
 		int count = 1;
 		for (int i = 0; i < 40; i++) {
 			if (p > q / 2) {
 				if (check(q)) return Integer.toString(count);
 				else return "impossible";
 			} else {
 				if (2 * p == q) return Integer.toString(count);
 				else {
 					if (q % 2 != 0) return "impossible";
 					q = q / 2;
 					++count;
 				}
 			}
 		}
 		return "impossible";
 	}
 
 	private static boolean check(long q) {
 		long t = q;
 		for (int i = 0; i < 40; i++) {
 			if (t % 2 != 0) return false;
 			if (t == 2) return true;
 			t = t / 2;
 		}
 		return true;
 	}
 }

package codejam;
 
 import java.io.*;
 import java.util.*;
 
 public class problem1A {
 	public static void main(String[] args) {
 		Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 		int t = in.nextInt();
 		in.nextLine();
 		for (int i = 1; i <= t; i++) {
 			//int n = in.nextInt();
 			//System.out.println("Case #" + i + ": " + ret(n));
 			System.out.println("Case #" + i + ": " + fkt1(in.nextLine()));
 		}
 	}
 	
 	static String fkt1(String w) {
 		
 		String r = Character.toString(w.charAt(0));
 		
 		for (int i = 1; i < w.length(); i++) {
 			if (w.charAt(i) >= r.charAt(0)) {
 				r = Character.toString(w.charAt(i)) + r;
 			}
 			else {
 				r += Character.toString(w.charAt(i));
 			}
 		}
 		
 		return r;
 	}
 }

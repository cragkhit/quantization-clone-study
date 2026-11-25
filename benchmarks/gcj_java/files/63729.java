import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Scanner;
 public class Fractiles {
 	private static String FileNameOut = "/home/lcc/workspace/CodeJam/result1.txt";
 	
 	//private static String FileNameIn = "/home/lcc/workspace/CodeJam/test.in";
 	private static String FileNameIn = "/home/lcc/workspace/CodeJam/D-large.in";
 	
 	public static void main(String[] args) {
 
 
 		try {
 			Scanner sc = new Scanner(new File(FileNameIn));
 
 			PrintWriter pw = new PrintWriter(FileNameOut);
 			int T = sc.nextInt();
 
 			for (int t = 0; t < T; t++) {
 				int K = sc.nextInt();
 				int C = sc.nextInt();
 				int S = sc.nextInt();
 				solve (t,K,C,S,pw);
 				
 			}
 			pw.flush();
 			pw.close();
 			sc.close();
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	private static void solve(int t, int K, int C, int S, PrintWriter pw) {
 		String a = generate(K,C);
 		
 		int countChange = 0;
 		ArrayList<Integer>al = new ArrayList<Integer>();
 		for (int i = 0; i < a.length()-1; i++) {
 			if ((a.charAt(i)=='L') && (a.charAt(i+1)=='G') ) {
 				countChange++;
 				al.add(i);
 				
 			}
 		}
 		System.out.printf("Case #%d: ",t+1);
 		pw.printf("Case #%d: ",t+1);
 		if (K == 1) {
 			System.out.println("1");
 			pw.println("1");
 		}else if (  (C==1)){
 			
 			if (S == K ) {
 				for (int i = 0; i < S; i++) {
 					System.out.print(i+1);
 					pw.print(i+1);
 					System.out.print(" ");
 					pw.print(" ");
 				}
 				pw.println();
 				System.out.println();
 			}else {
 				System.out.println("IMPOSSIBLE");
 				pw.println("IMPOSSIBLE");
 			}
 			
 		}else {
 			if (S >= countChange) {
 				if (countChange == 1) {
 					System.out.print(al.get(0)+2);
 					pw.print(al.get(0)+2);
 				}else {
 					System.out.print(al.get(0)+1);
 					pw.print(al.get(0)+1);
 				}
 				for (int i = 1; i < countChange; i++) {
 					System.out.print(" ");
 					System.out.print(al.get(i)+2);
 					pw.print(" ");
 					pw.print(al.get(i)+2);
 				}
 				System.out.println();
 				pw.println();
 			}else {
 				System.out.println("IMPOSSIBLE");
 				pw.println("IMPOSSIBLE");
 			}
 		}
 		
 	}
 
 	private static String generate(int k, int c) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (int i = 0; i<k-1; i++) {
 			sb.append("L");
 		}
 		sb.append("G");
 		return (buildC(sb.toString(),k-1, c));
 		
 	}
 
 	private static String buildC(String s, int size, int c) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < size; i++) {
 			sb.append(s);
 		}
 		return sb.toString();
 		
 	}
 
 	private static String fillWithG(String s) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < s.length(); i++) {
 			sb.append('G');
 		}
 		return sb.toString();
 	}
 }

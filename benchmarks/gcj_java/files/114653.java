package qualification_round_2013;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class B {
 
 	public void run() {
 		try {
 			Scanner in = new Scanner(new File("B.in"));
 			PrintWriter out = new PrintWriter(new File("B.txt"));
 			int T = in.nextInt();
 			int R = 9;
 			for (int TC = 1; TC <= T; TC++) {
 				int N = in.nextInt(), M = in.nextInt();
 				int[][] board = new int[N][M];
 				for (int i = 0; i < N; i++) {
 					for (int j = 0; j < M; j++) {
 						board[i][j] = in.nextInt();
 						if (TC == R) {
 							System.out.print(" " + board[i][j]);
 						}
 					}
 					if (TC == R) System.out.println();
 				}
 				boolean sol = solve(board, N, M);
 				out.println("Case #" + TC + ": " + (sol ? "YES" : "NO"));
 				System.out.println("Case #" + TC + ": " + (sol ? "YES" : "NO"));
 			}
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean solve(int[][] board, int N, int M) {
 		boolean[][] vis = new boolean[N][M];
 		for (int i = 0; i < N; i++) {
 			int max = 0;
 			for (int j = 0; j < M; j++) {
 				max = Math.max(board[i][j], max);
 			}
 			for (int j = 0; j < M; j++) {
 				if (board[i][j] == max) {
 					vis[i][j] = true;
 				}
 			}
 		}
 		for (int i = 0; i < M; i++) {
 			int max = 0;
 			for (int j = 0; j < N; j++) {
 				max = Math.max(board[j][i], max);
 			}
 			for (int j = 0; j < N; j++) {
 				if (board[j][i] == max) {
 					vis[j][i] = true;
 				}
 			}
 		}
 		for (int i = 0; i < N; i++) {
 			for (int j = 0; j < M; j++) {
 				if (!vis[i][j]) return false;
 			}
 		}
 		return true;
 	}
 	
 	public static void main(String[] args) {
 		new B().run();
 	}
 }

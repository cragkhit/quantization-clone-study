import java.util.Scanner;
 
 
 public class B {
 	
 	public static final int limit = 1000;
 	
 	public static void go() {
 		Scanner sc = new Scanner(System.in);
 		int T = sc.nextInt();
 		for(int i = 1; i <= T; i++) {
 			int D = sc.nextInt();
 			int[] pancakes = new int[D];
 			for(int j = 0; j < D; j++) {
 				pancakes[j] = sc.nextInt();
 			}
 			int result = solve(pancakes);
 			System.out.println("Case #" + i + ": " + result);
 		}
 		sc.close();
 	}
 	
 	public static int solve(int[] pancakes) {
 		int minResult = Integer.MAX_VALUE;
 		for (int i = 1; i <= limit; i++) {
 			int result = i;
 			for(int j = 0; j < pancakes.length; j++) {
 				result += (pancakes[j] - 1) / i;
 			}
 			if(minResult > result) {
 				minResult = result;
 			}
 		}
 		return minResult;
 	}
 	
 	public static void main(String[] args) {
 		B.go();
 	}
 }

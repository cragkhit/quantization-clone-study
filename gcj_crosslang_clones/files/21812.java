import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.StringTokenizer;
 
 public class Pancakes {
 
 	public static void main(String[] args) throws Exception {
 		
 		BufferedReader in = new BufferedReader(new FileReader("pancakes.in"));
 		int T = Integer.parseInt(in.readLine());
 		int D;
 		int[] c;
 		StringTokenizer st;
 		for (int i = 0; i < T; i++) {
 			D = Integer.parseInt(in.readLine());
 			st = new StringTokenizer(in.readLine());
 			c = new int[D];
 			for (int j = 0; j < c.length; j++) {
 				c[j] = Integer.parseInt(st.nextToken());
 			}
 			System.out.println("Case #" + (i+1) + ": " + solve(D, c));
 		}
 	}
 	
 	static int solve(int D, int[] c){
 		int sol = 1001, days;
 		int maxDays = 0;
 		for (int i = 0; i < c.length; i++) {
 			maxDays = Math.max(maxDays, c[i]);
 		}
 		for (int eatingDays = 1; eatingDays <= maxDays; eatingDays++) {
 			days = eatingDays;
 			for (int i = 0; i < c.length; i++) {
 				days+=c[i]%eatingDays==0?c[i]/eatingDays-1:c[i]/eatingDays;
 			}
 			sol = Math.min(sol, days);
 		}
 		return sol;
 	}
 }

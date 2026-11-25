import java.math.BigDecimal;
 import java.util.Scanner;
 
 
 public class Bullseye {
 
 	public void solve(int caseNo, long rl , long tl){
 		if(Math.sqrt((double)Long.MAX_VALUE) < rl){
 			long k = 0;
 			while(2*k*k + (2*rl-1) * k - tl < 0){
 				k++;
 			}
 			System.out.printf("Case #%d: %d\n", caseNo, --k);
 			return;
 		}
 		
 		double r = (double) rl;
 		double t = (double) tl;
 		double k = Math.sqrt(4*r*r - 4*r +1 + 8*t);
 		k = 1 - 2*rl + k;
 		k = k /4;
 		System.out.printf("Case #%d: %d\n", caseNo, (long) k);
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Bullseye f = new Bullseye();
 		Scanner sc = new Scanner(System.in);
 		int totalCase = Integer.parseInt(sc.nextLine());
 		for(int i =1; i <= totalCase; i++){
 			String s = sc.nextLine();
 			long r = Long.valueOf(s.split(" ")[0]);
 			long t = Long.valueOf(s.split(" ")[1]);
 			f.solve(i, r, t);
 		}
 	}
 
 }

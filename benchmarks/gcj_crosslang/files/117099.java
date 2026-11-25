import java.util.*;
 public class A {
 	public static void main (String [] arg) {
 		Scanner sc = new Scanner(System.in);
 		int T = Integer.parseInt(sc.nextLine());
 		for (int ii = 1; ii<=T; ++ii) {
 			
 			int N = sc.nextInt();
 			if ( N == 0 ) {
 				System.out.printf("Case #%d: INSOMNIA\n", ii);
 				continue;
 			}
 			
 			int MASK = 0;
 			int i = 1;
 			for (; MASK != (1<<10)-1; i++)
 				MASK |= A.getMask(i*N);
 			
 			System.out.printf("Case #%d: %s\n",ii,(i-1)*N);
 		}
 	}
 	
 	public static int getMask (int val) {
 		int MASK = 0;
 		while (val > 0) {
 			MASK = MASK | (1<< (val % 10));
 			val /= 10;
 		}
 		return MASK;
 	}
 }
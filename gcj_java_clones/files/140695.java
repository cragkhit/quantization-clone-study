import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class EpreuveA {
 	
 	public static void solve() throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String input;
 		String[] split;
 		input = br.readLine();
 		int nb_pb = Integer.valueOf(input);
 		
 		long v = 1;
 		v <<= 40;
 
 		for (int i = 1; i <= nb_pb; i++) {
 			
 			input = br.readLine();
 			split = input.split("/");
 			long p = Long.valueOf(split[0]);
 			long q = Long.valueOf(split[1]);
 			
 			long total = p * v / q;
 			Double result = Math.floor(Math.log(total) / Math.log(2));
 			int r = 40 - result.intValue();
 			
 			if (total * q == p * v)
 				System.out.printf("Case #%d: %d\n", i, r);
 			else
 				System.out.printf("Case #%d: %s\n", i, "impossible");
 			
 			
 		}
 		
 	}
 
 }

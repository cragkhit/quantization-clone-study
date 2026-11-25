import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 public class Fractiles4 {
 
 	
 	public static void main(String[] args) throws Exception {
 
 
 
 
 		FileInputStream is = new FileInputStream(new File(args[0]));
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 		String line = br.readLine();
 		int testCase = 1;
 
 		while ((line = br.readLine()) != null) {
 			
 			String[] parameters = line.split(" ");
 			int K = Integer.parseInt(parameters[0]);
 			int C = Integer.parseInt(parameters[1]);
 			int S = Integer.parseInt(parameters[2]);
 
 			if (K == 1) {
 
 				if (S >= 1) {
 					System.out.format("Case #%d: 1\n", testCase);
 				} else {
 					System.out.format("Case #%d: IMPOSSIBLE\n", testCase);
 				}
 				testCase++;
 				continue;				
 			}
 			
 			if (C == 1) {
 				
 				if (S >= K) {
 					System.out.format("Case #%d:", testCase);
 					for (int i = 1; i <= K; i++) {
 						System.out.print(" " + i);
 					}
 					System.out.println("");
 				} else {
 					System.out.format("Case #%d: IMPOSSIBLE\n", testCase);
 				}
 				testCase++;
 				continue;											
 			}
 			
 			if (S < K - 1) {
 				System.out.format("Case #%d: IMPOSSIBLE\n", testCase);
 				testCase++;
 				continue;		
 			}
 			
 			
 			System.out.format("Case #%d:", testCase);
 			for (int i = 2; i <= K; i++) {
 				System.out.print(" " + i);
 			}
 			System.out.println("");
 			testCase++;
 			
 		}
 		
 		br.close();
 						
 	}
 	
 	
 }

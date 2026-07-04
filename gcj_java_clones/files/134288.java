import java.io.FileReader;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class Lawn {
 	
 	public void solve(Scanner scan, PrintWriter out) {
 		int n = scan.nextInt();
 		int m = scan.nextInt();
 		
 		int[][] array = new int[n][m];
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < m; j++) {
 				array[i][j] = scan.nextInt();
 			}
 		}
 		
 		for (int i = 0; i < n; i++) {
 			int smallest = 100;
 			for (int j = 0; j < m; j++) {
 				if (array[i][j] < smallest) {
 					smallest = array[i][j];
 				}
 			}
 			boolean value = true;
 			for (int j = 0; j < m; j++) {
 				if (array[i][j] != smallest) {
 					value = false;
 					break;
 				}
 			}
 			if (!value) {
 				for (int j = 0; j < m; j++) {
 					if (array[i][j] == smallest) {
 						for (int k = 0; k < n; k++) {
 							if (array[k][j] > smallest) {
 								System.out.println("NO");
 								out.println("NO");
 								return;
 							}
 						}
 					}
 				}
 			}
 		}
 		System.out.println("YES");
 		out.println("YES");
 	}
 
 	public static void main(String[] args) throws Exception {
         Scanner scan = new Scanner(new FileReader("input.in"));
         PrintWriter out = new PrintWriter("output.txt");
         int problems = scan.nextInt();
         for (int count = 0; count < problems; count++) {
             System.out.print("Case #" + (count+1) + ": ");
             out.print("Case #" + (count+1) + ": ");
             new Lawn().solve(scan, out);
         }
         out.flush();
         out.close();
         scan.close();
     }
 	
 }
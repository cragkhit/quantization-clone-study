import java.util.*;
 import java.io.*;
 
 public class Lawn {
 	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 	public static void main(String[] args) throws IOException {
 		int T = Integer.parseInt(in.readLine());
 		int testCase = 1;
 		while(testCase <= T) {
 			int N, M;
 			String line = in.readLine();
 			String[] toks = line.split(" ");
 			N = Integer.parseInt(toks[0]);
 			M = Integer.parseInt(toks[1]);
 			int[][] lawn = new int[N][M];
 			for(int i=0; i<N; i++){
 				line = in.readLine();
 				toks = line.split(" ");
 				for(int j=0; j<M; j++){
 					lawn[i][j] = Integer.parseInt(toks[j]);
 				}
 			}
 			
 			boolean ok = true;
 			for(int i=0;i<N;i++)
 			for(int j=0;j<M;j++){
 				boolean canCut = true;
 				//check the row
 				for(int k=0;k<M;k++){
 					if(lawn[i][j]<lawn[i][k]){
 						canCut = false;
 						break;
 					}
 				}
 				//check the col
 				if(!canCut){
 					boolean colCut = true;
 					for(int k=0;k<N;k++){
 						if(lawn[i][j]<lawn[k][j]){
 							colCut = false;
 							break;
 						}
 					}
 					if(colCut) canCut = true;
 				}
 				if(!canCut){
 					ok = false;
 					break;
 				}
 			}
 			
 			if(ok){
 				System.out.println("Case #" + testCase + ": YES");
 			}else{
 				System.out.println("Case #" + testCase + ": NO");
 			}
 			testCase++;
 		}
 	}
 }

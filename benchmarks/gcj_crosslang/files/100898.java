import java.util.Scanner;
 
 public class CountingSheep {
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Scanner input = new Scanner(System.in);
 		int t = input.nextInt();
 		for(int a = 0; a  < t; a++){
 			long k = input.nextInt();
 			long k2 = k;
 			boolean[] out = new boolean[10];
 			int start = 0;
 			while(start < 1000000){
 				start++;
 				String stuff = k+"";
 				k = k + k2;
 				
 				charAdd(out, stuff);
 				if(allTrue(out)){
 					break;
 				}
 			}
 			if(start == 1000000){
 				System.out.println("Case #"+(a+1)+":" + " INSOMNIA");
 			}
 			else{
 				System.out.println("Case #"+(a+1)+":" + " " + (k-k2));
 			}
 			
 		}
 	}
 
 	private static void charAdd(boolean[] out, String stuff) {
 		for(int a = 0; a < stuff.length(); a++){
 			out[stuff.charAt(a)-'0'] = true;
 		}
 	}
 	private static boolean allTrue(boolean[] out){
 		boolean output = true;
 		for(int a =0; a< out.length; a++){
 			output = output && out[a]; 
 		}
 		return output;
 	}
 }

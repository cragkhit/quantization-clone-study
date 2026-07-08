 import java.io.*;
  import java.util.*;
 
 public class problemA{
 	public static void main(String[] args){
 		int t, i, j, k, lengthOfS;
 		String s, letter;
 		int[] numbers = new int[10];
 
 		Scanner fileIn = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 		t = fileIn.nextInt();
 
 
 		for(i = 1; i <= t; i++){
 
 			for(j = 0; j < 10; j++){
 				numbers[j] = 0;
 			}
 
 			s = fileIn.next();
 			lengthOfS = s.length();
 			for(j = 0; j < lengthOfS ; j++){
 				letter = s.substring(j, j+1);
 				switch(letter){
 					case "Z" :
 						numbers[0]++;
 						break;
 					case "W" :
 						numbers[2]++;
 						break;
 					case "U" :
 						numbers[4]++;
 						break;
 					case "X":
 						numbers[6]++;
 						break;
 					case "G":
 						numbers[8]++;
 						break;
 					case "O":
 						numbers[1]++;
 						break;
 					case "R":
 						numbers[3]++;
 						break;
 					case "F":
 						numbers[5]++;
 						break;
 					case "S":
 						numbers[7]++;
 						break;
 					case "I":
 						numbers[9]++;
 						break;
 
 
 				}
 
 
 			}
 			
 			numbers[1] -= (numbers[0] + numbers[2] + numbers[4]);
 			numbers[3] -= (numbers[0] + numbers[4]);
 			numbers[5] -= numbers[4];
 			numbers[7] -= numbers[6];
 			numbers[9] -= (numbers[8] + numbers[6] + numbers[5]);
 			System.out.print("Case #" + i + ": ");
 
 			for(j = 0; j < 10; j++){
 				for(k = 0; k < numbers[j]; k++){
 					System.out.print(j);
 				}
 
 			}
 			System.out.println();
 		}
 
 	}
 }
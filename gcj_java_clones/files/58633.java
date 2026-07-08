import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 
 public class main {
 	public static void main(String[] args) throws FileNotFoundException {
 		File f =new File("../input.in");
 		Scanner sc = new Scanner(f);
 		String result;
 		char[] input;
 		boolean isOver, finished;
 		
 		int T = sc.nextInt();sc.nextLine();
 		int[] matrix = new int[16];
 		for (int i=1; i<=T; i++){
 			matrix = new int[16];
 			isOver=false;
 			finished=false;
 			result="Case #"+i+": ";
 			for (int j=0; j<4; j++){
 				input=sc.nextLine().toCharArray();
 				System.out.println(input);
 				System.out.println(j);
 				for (int k=0;k<4;k++){
 					if(input[k]=='.'){
 						isOver=true;
 						matrix[4*j+k]=0;
 					}
 					else if (input[k]=='X'){
 						matrix[4*j+k]=2;
 					}
 					else if (input[k]=='O'){
 						matrix[4*j+k]=3;
 					}
 					else
 						matrix[4*j+k]=1;
 				}
 			}	
 			
 			for (int j=0; j<4; j++){
 				if(matrix[4*j]*matrix[4*j+1]*matrix[4*j+2]*matrix[4*j+3]==16 || matrix[4*j]*matrix[4*j+1]*matrix[4*j+2]*matrix[4*j+3]==8){
 					result+="X won"; finished=true;
 					break;
 				}
 				if(matrix[4*j]*matrix[4*j+1]*matrix[4*j+2]*matrix[4*j+3]==81 || matrix[4*j]*matrix[4*j+1]*matrix[4*j+2]*matrix[4*j+3]==27){
 					result+="O won"; finished=true;
 					break;
 				}
 			}
 			if(!finished){
 				for (int j=0; j<4; j++){
 					if(matrix[j]*matrix[j+4]*matrix[j+8]*matrix[j+12]==16 || matrix[j]*matrix[j+4]*matrix[j+8]*matrix[j+12]==8){
 						result+="X won"; finished=true;
 						break;
 					}
 					if(matrix[j]*matrix[j+4]*matrix[j+8]*matrix[j+12]==81 || matrix[j]*matrix[j+4]*matrix[j+8]*matrix[j+12]==27){
 						result+="O won"; finished=true;
 						break;
 					}
 				}
 			}
 			
 			if(!finished){
 				
 					if(matrix[0]*matrix[5]*matrix[10]*matrix[15]==16 || matrix[0]*matrix[5]*matrix[10]*matrix[15]==8){
 						result+="X won"; finished=true;
 					}
 					
 					if(matrix[0]*matrix[5]*matrix[10]*matrix[15]==81 || matrix[0]*matrix[5]*matrix[10]*matrix[15]==27){
 						result+="O won"; finished=true;
 						
 					}
 				
 			}
 					
 					if(!finished){
 						
 						if(matrix[3]*matrix[6]*matrix[9]*matrix[12]==16 || matrix[3]*matrix[6]*matrix[9]*matrix[12]==8){
 							result+="X won"; finished=true;
 						}
 						
 						if(matrix[3]*matrix[6]*matrix[9]*matrix[12]==81 || matrix[3]*matrix[6]*matrix[9]*matrix[12]==27){
 							result+="O won"; finished=true;
 							
 						}
 					
 				}
 			
 			if(!finished){
 				if(!isOver)
 					result+="Draw";
 				else
 					result+="Game has not completed";
 				 
 			}
 			ecrire("../result1.txt", result+"\n");
 			sc.nextLine();	
 		}
 	}
 		
 
 
 	
 	public static void ecrire(String pathTo, String tab) {
 		try {
 			FileWriter fos = new FileWriter(pathTo, true);
 			BufferedWriter bufferWritter = new BufferedWriter(fos);
 			bufferWritter.write(tab);
 	        bufferWritter.close();
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 	
 
 }

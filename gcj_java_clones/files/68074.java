package codeJam2016;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class Pancakes {	
 	
 	public static int findManeuvers(String s){
 		
 		int result=0;
 		char compareChar = '-';
 		for(int i=s.length()-1;i>=0;i--){			
 			//If you find the char
 			if(s.charAt(i)==compareChar){
 				result++;
 				//flip the char
 				if(compareChar=='-'){
 					compareChar='+';
 				}
 				else{
 					compareChar='-';
 				}
 			}						
 		}
 		return result;
 	}
 	
 	public static void main(String args[]){
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));		
 		try{
 			int tc = Integer.parseInt(br.readLine());
 			int[] result = new int[tc];
 			for(int i=0;i<tc;i++){
 				String s = br.readLine();				
 				result[i] = findManeuvers(s);
 			}
 			
 			//Print results
 			int j=0;
 			for(int i=0;i<tc;i++){
 				j=i+1;
 				System.out.println("Case #" + j + ": " + result[i]);
 			}
 		}
 		catch(IOException e){
 			System.out.println("IOException" + e);
 		}
 		catch(NumberFormatException e){
 			System.out.println("NumberFormatException " + e);
 		}
 		catch(Exception e){
 			System.out.println("Exception " + e);
 		}
 	}
 
 }

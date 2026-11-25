package round1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 public class Problem1 {
 
 	public static void main(String[] args) {
 		String fileName = args[0];
 		String outputFileName = args[1];
 		StringBuffer outputBuffer = new StringBuffer();
 		BufferedReader br = null;
 		try {
 
 			br = new BufferedReader(new FileReader(fileName));
 			int noOfTestCases = Integer.parseInt(br.readLine().trim());
 			for (int i=1; i<=noOfTestCases; i++) {
 
 				String line = br.readLine();
 				String sorted = sort(line);
 				
 				String output = "Case #" + i + ": ";
 				outputBuffer.append(output);
 				outputBuffer.append(sorted);
 				outputBuffer.append("\n");
 				
 			}
 			writeOutPut(outputFileName, outputBuffer);
 
 		
 		}catch(Exception e) {
 			
 			e.printStackTrace();
 		}finally {
 			
 			try {
 				br.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		}
 
 	}
 	
 	private static String sort(String s) {
 		
 	
 		String currentChar = "";	
 		String sorted = "";
 		//String nextChar = s.substring(1, 2);
 		
 		while(s.length() > 0){
 			currentChar = s.substring(0, 1);
 			if (sorted.length() > 0) {
 				String sortedFirstChar = String.valueOf(sorted.charAt(0));
 				//String sortedLastChar = String.valueOf(sorted.charAt(sorted.length()-1));
 				
 				if (sortedFirstChar.compareTo(currentChar) > 0) {
 					sorted = sorted + currentChar;
 				}else {
 					sorted = currentChar + sorted;;
 				}
 			}else {
 				sorted = currentChar;
 			}			
 			
 			s=s.substring(1);
 			
 		}
 		
 		return sorted;
 		
 		
 	}
 	
 	
 	
 	private static void writeOutPut(String fileName, StringBuffer outputBuffer) {
 
 		BufferedWriter br = null;		
 		try {
 
 			br = new BufferedWriter(new FileWriter(fileName));
 			br.write(outputBuffer.toString().trim());
 		} catch(Exception e) {
 			
 			e.printStackTrace();
 		} finally {
 			
 			try {
 				br.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 
 
 }

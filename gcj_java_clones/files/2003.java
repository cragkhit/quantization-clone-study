package com.devadatta.codejam;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 public class CodeJam1 {
 
 	public static void main(String[] args) throws Exception {
 		String fileLocation = "/home/srinivasu/Downloads/";
 		String fileName = "A-large";
 		// String fileName = "A-small-practice";
 		// String fileName = "A-large-practice";
 		
 		FileReader fileReader = new FileReader(fileLocation + fileName + ".in");
 		BufferedReader bufferedReader = new BufferedReader(fileReader);
 
 		File file = new File(fileLocation + fileName + ".out");
 		file.createNewFile();
 		FileWriter writer = new FileWriter(file);
 		
 		int loop = Integer.parseInt(bufferedReader.readLine());
 		
 		for (int counter = 1; counter <= loop; counter++) {
 			String input =  bufferedReader.readLine();
 			String out= "";
 			for(int i=0;i<input.length();i++){
 				if(i==0){
 					out= out+input.charAt(i);
 					continue;
 				}
 				if(input.charAt(i) < out.charAt(0)){
 					out = out+input.charAt(i);
 				}else
 				{
 					out = input.charAt(i)+out;
 				}
 			}
 			
 			writer.write("Case #" + counter + ": "+out+"\n");
 			System.err.print("Case #" + counter + ": "+out+"\n");
 		}
 
 		writer.flush();
 		writer.close();
 		bufferedReader.close();
 
 	}
 }

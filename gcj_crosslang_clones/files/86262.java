import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 
 public class Omino {
 	
 	public static String LOSE = "GABRIEL";
 	public static String WIN = "RICHARD";
 
 	public static void generateOutput (ArrayList<String> TestCases) 
 	{
 		String outFileName = "C:\\dev\\projects\\GCJ2015\\QR_4_Omino\\src\\output.txt";
 		try { 
         File file = new File(outFileName);
         BufferedWriter output = new BufferedWriter(new FileWriter(file));
         for (int i =0; i < TestCases.size(); i++) {
         	output.write("Case #" + (i+1) + ": " + TestCases.get(i) + "\r\n");
         }
         
         output.close();
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		} 
 		
 	}
 	
 	public static String getAnswer(int X, int R, int C) {
 		if (X==1) {
 			return LOSE;
 		}
 		
 		
 		int numberOfSquares = R * C;
 		if ((numberOfSquares % X) != 0) {
 			//impossible to fit several objects
 			return WIN;
 		}
 		
 		int minSide = X / 2;
 		
 		if ((minSide >= R) || (minSide >=C)) {
 			if (X != 2)
 			{
 				return WIN;
 			}
 		}
 		
 		if ((X > R) && (X >C)) {
 			return WIN;
 		}
 		
 
 		
 		return LOSE;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		ArrayList<String> TestCases;
 		
 		TestCases = new ArrayList<String>();
 		
 		String fileName = "C:\\dev\\projects\\GCJ2015\\QR_4_Omino\\src\\Example.txt";
 		
 		try 
 		{
 			InputStream in = new FileInputStream(new File(fileName));
 			Scanner reader = new Scanner(in);
 	        
 
 	        int cntTests = reader.nextInt();
 	        
 	        for (int i = 0; i<cntTests; i++) 
 	        {
 	        	int X = reader.nextInt();
 	        	int R = reader.nextInt();
 	        	int C = reader.nextInt();
 	        	
 	        	String res = getAnswer(X, R, C);
 		        	
 	        	TestCases.add(res);
 	        }
 	         
 	        reader.close();
 		} catch (Exception ex)	{
 			System.out.print(ex.getMessage());
 		}
 		
 		generateOutput(TestCases);
 
 	}
 }

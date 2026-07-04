import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class QualificationC {
 
 	private static String inFile="C-large.in";
 	private static String outFile="C-largeout.txt";
 	private static String encoding = "UTF-8";
 	private static char[][] quaternionTable= {{'1','i','j','k','l','m','n','o'},{'i','l','k','n','m','1','o','j'},{'j','o','l','i','n','k','1','m'},{'k','j','m','l','o','n','i','1'},{'l','m','n','o','1','i','j','k'},{'m','1','o','j','i','l','k','n'},{'n','k','1','m','j','o','l','i'},{'o','n','i','1','k','j','m','l'}};
 	public static void main(String[] args) {
 		try {
 			solve();
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 	}
 
 	private static void solve() throws IOException {
 		Scanner scanner = new Scanner(new FileInputStream(inFile), encoding);
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 		int nCases = Integer.parseInt(scanner.nextLine());
 
 		for (int i = 0; i < nCases; i++) {
 			System.out.println("Case #" + (i + 1) + " start");
 			String val = solveCase(scanner);
 			writer.write("Case #" + (i + 1) + ": " + val + "\n");
 			System.out.println("Case #" + (i + 1) + " result: "+val);
 		}
 		writer.close();
 	}
 
 	private static String solveCase(Scanner input) {
 		String[] caseValues = input.nextLine().split(" ");
 		int L = Integer.parseInt(caseValues[0]);
 		long X = Long.parseLong(caseValues[1]);
 		char[] testString = input.nextLine().toCharArray();
 		char testStringvalue = '1';
 		char currentValue;
 		char lookingFor = 'i';
 		char restOfString = 'l';
 		
 		System.out.println(" String is " + X + "x " + String.valueOf(testString));
 		
 		for (int i=0; i<testString.length;i++){
 			testStringvalue=quaternionMultiply(testStringvalue,testString[i]);
 			if (testStringvalue==lookingFor){
 				switch(lookingFor){
 				case 'i': 
 					System.out.println(" Found i at point " + i);
 					restOfString='m';
 					lookingFor='k';
 					break;
 				case 'k':
 					System.out.println(" Found j at point " + i);
 					restOfString='o';
 					lookingFor='l';
 					break;
 				case 'l':
 					System.out.println(" Found k at point " + i);
 					restOfString='l';
 					lookingFor='a';
 					break;
 				}
 			}
 		}
 		
 		currentValue=testStringvalue;
 		
 		for (int j=0;j<4&&X>1;){
 			for (int i=0; i<testString.length;i++){
 				currentValue=quaternionMultiply(currentValue,testString[i]);
 				if (currentValue==lookingFor){
 					switch(lookingFor){
 					case 'i': 
 						System.out.println(" Found i at point " + (i+j*testString.length));
 						restOfString='m';
 						lookingFor='k';
 						break;
 					case 'k':
 						System.out.println(" Found j at point " + (i+j*testString.length));
 						restOfString='o';
 						lookingFor='l';
 						break;
 					case 'l':
 						System.out.println(" Found k at point " + (i+j*testString.length));
 						restOfString='l';
 						lookingFor='a';
 						break;
 					}
 				}
 			}
 			j++;
 			X--;
 		}
 		
 		System.out.println(" String is equal to " + testStringvalue);
 		restOfString = quaternionMultiply(restOfString,currentValue);
 		for(int i = 1;i<X%4;i++){
 			restOfString = quaternionMultiply(restOfString,testStringvalue);
 		}
 		System.out.println(" Rest of string is equal to " + restOfString);
 		
 		if ((lookingFor=='a'&&restOfString=='1')||(lookingFor=='l'&&restOfString=='k')){
 			return "YES";
 		}	
 		return "NO";
 	}
 	
 	private static char quaternionMultiply(char s1, char s2){
 		int i=0,j=0;
 		switch (s1){
 		case '1': i=0; break;
 		case 'i': i=1; break;
 		case 'j': i=2; break;
 		case 'k': i=3; break;
 		case 'l': i=4; break;
 		case 'm': i=5; break;
 		case 'n': i=6; break;
 		case 'o': i=7; break;
 		}
 		switch (s2){
 		case '1': j=0; break;
 		case 'i': j=1; break;
 		case 'j': j=2; break;
 		case 'k': j=3; break;
 		case 'l': j=4; break;
 		case 'm': j=5; break;
 		case 'n': j=6; break;
 		case 'o': j=7; break;
 		}
 		return quaternionTable[i][j];
 		}
 		
 	}
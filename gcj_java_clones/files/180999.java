package gcj;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 
 public class Test {
 
     private static String inputFileName = "input.in";
     private static String outputFileName = "F:\\SpringWorkspace\\gcj\\src\\gcj\\output.out";
     private static ClassLoader classLoader;
 
     static {
         classLoader = Test.class.getClassLoader();
     }
 
     private static void writeOutputToFile(String str) {
         Path file = Paths.get(outputFileName);
         try {
             Files.write(file, str.getBytes());
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
 
         try {
             StringBuilder output = new StringBuilder();
 
             String filePath = "F:\\SpringWorkspace\\gcj\\src\\gcj\\input.in";
             BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
             String strLine;
 
             int lineNumber = 0;
             int noOfTestCases = -1;
             int activeTestCaseNumber = 0;
             while ((strLine = reader.readLine()) != null) {
 
                 if (lineNumber == 0) {
                     noOfTestCases = Integer.parseInt(strLine);
                 } else {
                     noOfTestCases ++;
                     activeTestCaseNumber ++;
                     String s=strLine;
                     char c[]=s.toCharArray();
                     ArrayList ar=new ArrayList();
                     for(int i=0;i<c.length;i++){
                     	if(c[i]=='Z'){
                     		c[i]='0';
                     		remove(c,'E');
                     		remove(c,'R');
                     		remove(c,'O');
                     		ar.add(0);
                     	}
                     	if(c[i]=='W'){
                     		c[i]='0';
                     		remove(c,'T');
                     		remove(c,'O');
                     		ar.add(2);
                     	}
                     	if(c[i]=='U'){
                     		c[i]='0';
                     		remove(c,'F');
                     		remove(c,'O');
                     		remove(c,'R');
                     		ar.add(4);
                     	}
                     	if(c[i]=='X'){
                     		c[i]='0';
                     		remove(c,'S');
                     		remove(c,'I');
                     		ar.add(6);
                     	}
                     	if(c[i]=='G'){
                     		c[i]='0';
                     		remove(c,'E');
                     		remove(c,'I');
                     		remove(c,'H');
                     		remove(c,'T');
                     		ar.add(8);
                     	}
                     }
                     for(int i=0;i<c.length;i++){
                     	if(c[i]=='O'){
                     		c[i]='0';
                     		remove(c,'N');
                     		remove(c,'E');
                     		ar.add(1);
                     	}
                     	if(c[i]=='R'){
                     		c[i]='0';
                     		remove(c,'T');
                     		remove(c,'H');
                     		remove(c,'E');
                     		remove(c,'E');
                     		ar.add(3);
                     	}
                     	if(c[i]=='F'){
                     		c[i]='0';
                     		remove(c,'I');
                     		remove(c,'V');
                     		remove(c,'E');
                     		ar.add(5);
                     	}
                     	if(c[i]=='S'){
                     		c[i]='0';
                     		remove(c,'E');
                     		remove(c,'V');
                     		remove(c,'E');
                     		remove(c,'N');
                     		ar.add(7);
 
                     	}
                     	
                     }
                     int nine=0;
                     for(int i=0;i<c.length;i++){
                     	if(c[i]=='N'){
             				nine++;
             			}
                     }
                     for(int i=0;i<nine/2;i++){
                     	ar.add(9);
                     }
 
                     StringBuilder sb=new StringBuilder();
                     Collections.sort(ar);
                     for(int i=0;i<ar.size();i++){
                     	sb.append(ar.get(i));
                     }
                     output.append("Case #").append(activeTestCaseNumber).append(": ").append(sb.toString());
                     output.append("\n");
                 }
                 lineNumber++;
             }
 
             writeOutputToFile(output.toString());
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public static void remove(char c[], char n){
     	for(int j=0;j<c.length;j++){
 			if(c[j]==n){
 				c[j]='0';
 				break;
 			}
 		}
     }
     
     public static String odd(String s, ArrayList ar){
     	if(s.contains("O")){
         	s=s.replaceFirst("O", "");
 //        	s=s.replaceFirst("N", "");
 //        	s=s.replaceFirst("E", "");
         	ar.add(1);
         }if(s.contains("R")){
 //        	s=s.replaceFirst("T", "");
 //        	s=s.replaceFirst("H", "");
         	s=s.replaceFirst("R", "");
 //        	s=s.replaceFirst("E", "");
 //        	s=s.replaceFirst("E", "");
         	ar.add(3);
         }if(s.contains("F")){
         	s=s.replaceFirst("F", "");
 //        	s=s.replaceFirst("I", "");
 //        	s=s.replaceFirst("V", "");
 //        	s=s.replaceFirst("E", "");
         	ar.add(5);
         }if(s.contains("S")){
         	s=s.replaceFirst("S", "");
 //        	s=s.replaceFirst("E", "");
 //        	s=s.replaceFirst("V", "");
 //        	s=s.replaceFirst("E", "");
 //        	s=s.replaceFirst("N", "");
         	ar.add(7);
         }
     	return s;
     }
     public static String even(String s, ArrayList ar){
     	if(s.contains("Z")){
     		s=s.replaceFirst("Z", "");
 //    		s=s.replaceFirst("E", "");
 //    		s=s.replaceFirst("R", "");
 //    		s=s.replaceFirst("O", "");
         	ar.add(0);
         }if(s.contains("W")){
         	s=s.replaceFirst("W", "");
 //        	s=s.replaceFirst("T", "");
 //        	s=s.replaceFirst("O", "");
         	ar.add(2);
         }if(s.contains("G")){
 //        	s=s.replaceFirst("E", "");
 //        	s=s.replaceFirst("I", "");
         	s=s.replaceFirst("G", "");
 //        	s=s.replaceFirst("H", "");
 //        	s=s.replaceFirst("T", "");
         	ar.add(8);
         }if(s.contains("X")){
 //        	s=s.replaceFirst("S", "");
 //        	s=s.replaceFirst("I", "");
         	s=s.replaceFirst("X", "");
         	ar.add(6);
         }if(s.contains("U")){
 //        	s=s.replaceFirst("F", "");
 //        	s=s.replaceFirst("O", "");
         	s=s.replaceFirst("U", "");
 //        	s=s.replaceFirst("R", "");
         	ar.add(4);
         }
     	return s;
     }
     
 
 }

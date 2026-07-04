package com.google.codejam;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Scanner;
 
 public class GettingDigits {
 //	static private final String INPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/sample_input.txt";
 //	static private final String OUTPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/sample_output.txt";
 
 //	static private final String INPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/s_input_attempt1.in";
 //	static private final String OUTPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/s_output_attempt1.txt";
 	
 	static private final String INPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/b_input.in";
 	static private final String OUTPUT = "G:/C-competitions/CodeJam/2016/Round 1B/Problem 1/b_output.txt";
 	
 	static private Scanner in;
 	static private int[] counts=new int[27];
 	
 	public static boolean allCharsVanished() {
 		for(int i=0;i<27;i++) {
 			if(counts[i]>0)
 				return false;
 		}
 		return true;
 	}
 	public static int getDigits(String S,int n){
 		int count=0;
 		switch(n) {
 			case 0:
 				count=counts['Z'-'A'];
 				counts['Z'-'A']=0;
 				counts['E'-'A']-=count;
 				counts['R'-'A']-=count;
 				counts['O'-'A']-=count;
 				break;
 			case 1:				
 				int countO=counts['O'-'A'],countN=counts['N'-'A'],countE=counts['E'-'A'];
 				if(countO<countE) {
 					if(countO<countN)
 						count=countO;
 					else
 						count=countN;					
 				}
 				else {
 					if(countE<countN)
 						count=countE;
 					else
 						count=countN;
 				}
 				counts['E'-'A']-=count;
 				counts['N'-'A']-=count;
 				counts['O'-'A']-=count;	
 				break;
 			case 2:
 				count=counts['W'-'A'];
 				counts['W'-'A']=0;
 				counts['T'-'A']-=count;				
 				counts['O'-'A']-=count;
 				break;				
 			case 3:
 				int countT=counts['T'-'A'],countH=counts['H'-'A'],countR=counts['R'-'A'];
 				countE=counts['E'-'A'];
 				
 				if((countE>=2)) {
 					count=0;
 					while(countE>=2){
 						if(countT!=0 && countH!=0 && countR!=0) {
 							countT--;countH--;countR--;
 							countE-=2;
 							count++;
 						}
 						else
 							break;
 					}
 					counts['E'-'A']=countE;
 					counts['H'-'A']=countH;
 					counts['R'-'A']=countR;
 					counts['T'-'A']=countT;
 				}					
 				break;
 			case 4:
 				count=counts['U'-'A'];
 				counts['U'-'A']=0;
 				counts['F'-'A']-=count;				
 				counts['O'-'A']-=count;
 				counts['R'-'A']-=count;
 				break;
 			case 5:
 				count=counts['F'-'A'];
 				counts['F'-'A']=0;
 				counts['I'-'A']-=count;				
 				counts['V'-'A']-=count;
 				counts['E'-'A']-=count;
 				break;
 			case 6:
 				count=counts['X'-'A'];
 				counts['X'-'A']=0;
 				counts['I'-'A']-=count;				
 				counts['S'-'A']-=count;
 				break;
 			case 7:
 				count=counts['V'-'A'];
 				counts['V'-'A']=0;
 				counts['N'-'A']-=count;				
 				counts['S'-'A']-=count;
 				counts['E'-'A']-=count;
 				counts['E'-'A']-=count;
 				break;
 			case 8:
 				count=counts['G'-'A'];
 				counts['G'-'A']=0;
 				counts['E'-'A']-=count;				
 				counts['I'-'A']-=count;
 				counts['H'-'A']-=count;
 				counts['T'-'A']-=count;
 				break;
 			case 9:
 				count=counts['I'-'A'];
 				counts['I'-'A']=0;
 				counts['E'-'A']-=count;				
 				counts['N'-'A']-=count;
 				counts['N'-'A']-=count;
 				break;
 		}
 	
 		return count;
 	}
 	public static void solve() {
 		String S = in.nextLine();
 		 
 		Arrays.fill(counts, 0);
 		
 		for(char c : S.toCharArray()) {
 			counts[c-'A']++;
 		}
 		
 		int[] cnt=new int[10];
 		for(int i=0;i<10;i+=2) {
 			cnt[i]=getDigits(S,i);			
 			if(allCharsVanished())
 				break;
 		}
 		for(int i=1;i<10;i+=2) {
 			cnt[i]=getDigits(S,i);			
 			if(allCharsVanished())
 				break;
 		}
 		if(!allCharsVanished()) {
 			System.err.println("error.");			
 		}
 		
 		for(int i=0;i<10;i++) {
 			while(cnt[i]>0) {
 				System.out.print(i);
 				cnt[i]--;
 			}			
 		}
 		System.out.println();
 	}
 	public static void main(String[] args) {
 		FileInputStream instream = null;  
 	      PrintStream outstream = null;  
 	     
 	      try {  
 	          instream = new FileInputStream(INPUT);  
 	          outstream = new PrintStream(new FileOutputStream(OUTPUT));  
 	          System.setIn(instream);  
 	          System.setOut(outstream);  
 	      } catch (Exception e) {  
 	          System.err.println("Error Occurred.");  
 	      }  
 	     
 	      in = new Scanner(System.in);  
 	      int T=Integer.parseInt(in.nextLine());
 	      
 	      for (int t=1;t<=T;t++) {  
 	    	  System.out.print("Case #"+t+": ");
 	    	  solve();	         	              	    
 	      }
 	      System.err.println("done.");
 	      in.close();
 	      return;
 	}
 
 }
 

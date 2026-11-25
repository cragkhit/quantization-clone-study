import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Scanner;
 
 public class Main {
 	public static PrintWriter out = null;
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		FileReader fr = null;
 		try {
 			fr = new FileReader("test");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		BufferedReader br = new BufferedReader(fr);
 		Scanner in = new Scanner(br);
 
 		try {
 			out = new PrintWriter("output.txt", "UTF-8");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		input(in);
 
 		out.close();
 	}
 
 	private static void input(Scanner in) {
 		int t = Integer.parseInt(in.nextLine());
 		
 		
 		for (int i = 0; i < t; i++) {
 			//String[] dataR = in.nextLine().split(" ");
 			//TODO: Edit data for each case
 			//===============================
 			int intervals= Integer.parseInt(in.nextLine());
 			String[] dataR = in.nextLine().split(" ");
 			int[] data= new int[dataR.length];
 			for(int j=0;j<dataR.length;j++){
 				data[j]=Integer.parseInt(dataR[j]);
 			}
 			
 			//===============================
 			out.print("Case #" + (i + 1) + ": ");
 			solve1(data);
 			solve2(data);
 		}
 
 	}
 
 
 	private static void solve2(int[] data) {
 		int max=0;
 		int count=0;
 		for(int i=1;i<data.length;i++){
 			if(data[i-1]-data[i]>max){
 				max=data[i-1]-data[i];
 			}
 		}
 		count=max*(data.length-1);
 		for(int i=0;i<data.length-1;i++){
 			if(data[i]<max){
 				count-=max-data[i];
 			}
 		}
 		out.println(count);
 	}
 
 	private static void solve1(int[] data) {
 		// TODO Auto-generated method stub
 		int count =0;
 		for(int i=1;i<data.length;i++){
 			if(data[i-1]>data[i]){
 				count+=data[i-1]-data[i];
 			}
 		}
 		out.print(count+" ");
 	}
 
 	private static void solve(String O) {
 
 
 	}
 
 
 
 
 }

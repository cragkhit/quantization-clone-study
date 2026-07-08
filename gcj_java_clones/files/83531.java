package RQ2013QC;
 
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class GoogleCodeJamSolution {
 
 	public static void main(String[] args) throws NumberFormatException, IOException {
 		
 		final String filePath = "src/RQ2013QC/";
 		final String inputFileName = "input";
 		final String outputFileName = "output";
 		final BufferedReader in = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath+inputFileName))));
 		final BufferedWriter out = new BufferedWriter(new FileWriter(filePath+outputFileName));
 		final InputHandler inputHandler = new InputHandler(in);		
 		final int T = inputHandler.readInt();
 		final Algo algo = new Algo(inputHandler);
 		for (int i = 0; i < T; i++) {
 			out.write("Case #"+(i+1)+": "+algo.solveNext()+"\n");
 		}
 		in.close();
 		out.close();
 	}
 	public static class InputHandler{
 		BufferedReader in;
 		InputHandler(BufferedReader in){
 			this.in = in;
 		}
 		String readLine(){
 			try {
 				return in.readLine();
 			} catch (Exception e) {e.printStackTrace();return null;}
 		}
 		int readInt(){
 			try {
 				return Integer.parseInt(in.readLine());
 			} catch (Exception e) {e.printStackTrace();return 0;}
 		}
 		long readLong(){
 			try {
 				return Long.parseLong(in.readLine());
 			} catch (Exception e) {e.printStackTrace();return 0;}
 		}
 		double readDouble(){
 			try {
 				return Double.parseDouble(in.readLine());
 			} catch (Exception e) {e.printStackTrace();return 0;}
 		}
 		int[] readIntArray(){
 			String s[] = null;
 			try {
 				s = in.readLine().split(" ");
 			} catch (Exception e1) {e1.printStackTrace();}
 			int [] arr = new int[s.length];
 			for (int i = 0; i < arr.length; i++) {
 				arr[i] = Integer.parseInt(s[i]);
 			}
 			return arr;
 		}
 		long[] readLongArray(){
 			String s[] = null;
 			try {
 				s = in.readLine().split(" ");
 			} catch (Exception e1) {e1.printStackTrace();}
 			long [] arr = new long[s.length];
 			for (int i = 0; i < arr.length; i++) {
 				arr[i] = Long.parseLong(s[i]);
 			}
 			return arr;
 		}
 		double[] readDoubleArray(){
 			String s[] = null;
 			try {
 				s = in.readLine().split(" ");
 			} catch (Exception e1) {e1.printStackTrace();}
 			double [] arr = new double[s.length];
 			for (int i = 0; i < arr.length; i++) {
 				arr[i] = Double.parseDouble(s[i]);
 			}
 			return arr;
 		}
 	}
 	
 	public static class Algo{
 		InputHandler inputHandler;
 		boolean init;
 		Algo(InputHandler inputHandler){
 			this.inputHandler = inputHandler;
 			init = false;
 		}
 		String solveNext(){
 			return solveNaive();
 		}
 		private String solveNaive() {
 			if(!init){
 				initNaive();
 			}
 			long[] t = inputHandler.readLongArray();
 			long a = t[0], b = t[1];
 			double ar = Math.sqrt(a), br = Math.sqrt(b);
 			int aa = (int) Math.ceil(ar), bb = (int) Math.floor(br);
 			int res = mem[bb] - mem[aa-1];
 			return ""+res;
 		}
 		
 		int[] mem;
 		final int arrSize = 10000001;//10000001;
 		private void initNaive() {
 			init = true;
 			mem = new int[arrSize];
 			mem[0] = 0;
 			for (int i = 1; i < mem.length; i++) {
 				mem[i] = mem[i-1];
 				if(isPal(i)){
 					long ii = (long)i * i;
 					if (isPal(ii)) {
 						System.out.println(ii+" : "+i+" : "+((double)i /10));
 						mem[i]++;
 					}
 				}
 			}
 		}
 		private boolean isPal(long ii){
 			char[] a = Long.toString(ii).toCharArray();
 			int j1 =0, j2=a.length-1;
 			while(j1 < j2){
 				if(a[j1] != a[j2]){
 					return false;
 				}
 				j1++;
 				j2--;
 			}
 			return true;
 		}
 	}
 	
 		
 	
 }
import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 
 
 public class Dijkstra {
 	
 	public int index (String s) {
 		String [] f = {"1","i","j","k","-1","-i","-j","-k"};
 		for (int i = 0; i < 8; i++) {
 			if (s.equals(f[i])) return i;
 		}
 		return -1;
 	}
 	
 	public String result (String a, String b) {
 		String [][] table = {{"1","i","j","k","-1","-i","-j","-k"},
 							{"i","-1","k","-j","-i","1","-k","j"},
 							{"j","-k","-1","i","-j","k","1","-i"},
 							{"k","j","-i","-1","-k","-j","i","1"},
 							{"-1","-i","-j","-k","1","i","j","k"},
 							{"-i","1","-k","j","i","-1","k","-j"},
 							{"-j","k","1","-i","j","-k","-1","i"},
 							{"-k","-j","i","1","k","j","-i","-1"}};
 		return table [index(a)][index(b)];
 	}
 	
 	public String iterate (String x) {
 		String current = "1";
 		for (int i = 0; i < x.length(); i++) {
 			current = result(current, x.charAt(i)+"");
 		}
 		return current;
 	}
 	
 	
 	
 	public static void main (String [] args) throws NumberFormatException, IOException {
 		
 		Dijkstra mint = new Dijkstra();
 		
 
 		FileReader fin = new FileReader ("DijkstraInput.txt");
 		BufferedReader read = new BufferedReader (fin);
 		FileWriter fout = new FileWriter ("DijkstraOutput.txt");
 		BufferedWriter bout = new BufferedWriter (fout);
 		PrintWriter pout = new PrintWriter (bout);
 		
 		//InputStreamReader in = new InputStreamReader (System.in);
 		//BufferedReader read = new BufferedReader (in);
 		
 		int t = Integer.parseInt(read.readLine());
 		for (int i = 0; i < t; i++) {
 			String s = read.readLine();
 			StringTokenizer st = new StringTokenizer (s);
 			int l = Integer.parseInt(st.nextToken());
 			long x = Long.parseLong(st.nextToken());
 			if (x > 4) {
 				x= 4 + (x%4);
 			}
 			String y = read.readLine();
 			String f = "";
 			for (int j = 0; j < x; j++) {
 				f = f + y;
 			}
 			
 			boolean valid = false;
 			for (int j = 0; j < l-1; j++) {
 				if (y.charAt(j) != y.charAt(j+1)) {
 					valid = true;
 					break;
 				}
 			}
 			if (!valid) {
 				pout.println ("Case #" + (i+1) + ": NO");
 				System.out.println ("Case #" + (i+1) + ": NO");
 				continue;
 			}
 			
 			boolean went = false;
 			boolean found = false;
 			String previ = "1";
 			String prevj = "1";
 			String givesyouimaybe = "";
 			String givesyoujmaybe = "";
 			String givesyoukmaybe = "";
 			for (int si = 1; si < f.length()-1; si++) {
 				givesyouimaybe = f.substring(0, si);
 				previ = mint.result(previ, f.charAt(si-1)+"");
 				if (!previ.equals("i")) {
 					continue;
 				}
 				else {
 					for (int sj = si+1; sj < f.length(); sj++) {
 						givesyoujmaybe = f.substring(si, sj);
 						prevj = mint.result(prevj, f.charAt(sj-1)+"");
 						if (!prevj.equals("j")) continue;
 						else {
 							givesyoukmaybe = f.substring(sj, f.length());
 							if (!mint.iterate(givesyoukmaybe).equals("k")) {}
 							else {
 								found = true;
 							}
 							went = true;
 							break;
 						}
 					}
 					break;
 				}
 			}
 			if (found) {
 				pout.println ("Case #" + (i+1) + ": YES");
 				System.out.println ("Case #" + (i+1) + ": YES");
 				if (!mint.iterate(f).equals("-1")) { 
 					System.out.println("CONTRA!!!! " +givesyouimaybe+ " " +givesyoujmaybe+ " " +givesyoukmaybe);
 					System.out.println (mint.result(mint.iterate(givesyouimaybe), mint.result(mint.iterate(givesyoujmaybe), mint.iterate(givesyoukmaybe))));
 					System.out.println (mint.iterate(givesyouimaybe));
 					System.out.println (mint.iterate(givesyoujmaybe));
 					System.out.println (mint.iterate(givesyoukmaybe));
 					System.out.println (y);
 					System.out.println (f);
 				}
 			}
 			else {
 				pout.println ("Case #" + (i+1) + ": NO");
 				System.out.println ("Case #" + (i+1) + ": NO");
 			}
 			
 		}
 		pout.close();
 		bout.close();
 		fout.close();
 	}
 }

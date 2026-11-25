package roundoneA;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class RankandFile {
 	public static void main(String args[]) throws FileNotFoundException{
 		Scanner in = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/B-small-attempt2.in")))));
 //	Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 //    int t = in.nextInt();  // Scanner has functions to read ints, longs, strings, chars, etc.
 //    in.nextLine();
 //    for (int i = 1; i <= t; i++){
 //    	String s = in.nextLine();
 //    	System.out.println("Case #" + i + ": " +count(s));
 //    }
 		int t = in.nextInt();
 		for (int i = 1; i <= t; i++){
 			int n = in.nextInt();
 			HashMap<Integer, Integer> map = new HashMap<>();
 			for (int j = 0; j < 2 * n *n - n; j ++){
 				int num = in.nextInt();
 				if (map.containsKey(num)){
 					map.put(num, map.get(num) + 1);
 				} else {
 					map.put(num, 1);
 				}
 			}
 	    	System.out.println("Case #" + i + ":" + findloss(map, n));
 		}
 	}
 	
 	
 	public static String findloss(HashMap<Integer, Integer> files, int n){
 		StringBuffer res = new StringBuffer();
 		int[] arr = new int[n];
 		int count = 0;
 		for(int key: files.keySet()){
 			if (files.get(key) % 2 != 0){
 				arr[count] = key;
 				count ++;
 			}
 		}
 		Arrays.sort(arr);
 		for (int i = 0; i < n; i ++){
 			res.append(" ");
 			res.append(arr[i]);
 		}
 		return res.toString();
 	}
 }

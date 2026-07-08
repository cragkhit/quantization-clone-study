import java.io.*;
 import java.util.*;
 
 public class OneA {
     public static void main(String[] args) throws IOException {
 	BufferedReader r = new BufferedReader(new FileReader(args[0]));
 	int T = Integer.parseInt(r.readLine());
 	for (int i=0; i < T; i++) {
 	    r.readLine();
 	    String[] split = r.readLine().split(" ");
 	    int[] input = new int[split.length];
 	    for (int j=0; j < split.length; j++)
 		input[j] = Integer.parseInt(split[j]);
 	    int res1 = solve1(input);
 	    int res2 = solve2(input);
 	    System.out.println("Case #"+(i+1)+": "+res1 + " "+ res2);
 	}
     }
 
     public static int solve1(int[] t) {
 	int total = 0;
 	for (int i=0; i < t.length-1; i++)
 	    if (t[i+1]<t[i])
 		total += t[i]-t[i+1];
 	return total;
     }
 
     public static int solve2(int[] t) {
 	int minRate = 0;
 	for (int i=0; i < t.length-1; i++)
 	    if (t[i+1]<t[i])
 		minRate = Math.max(minRate, t[i]-t[i+1]);
 	int minEaten = 0;
 	for (int i=0; i < t.length-1; i++)
 	    if (t[i] < minRate)
 		minEaten += t[i];
 	    else
 		minEaten += minRate;
 	return minEaten;
     }
 }

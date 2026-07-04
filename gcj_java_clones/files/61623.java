import java.util.*;
 import java.io.*;
 
 public class FairAndSquare {
 	public static void main(String[] args) throws Exception{
 		Scanner reader = new Scanner(System.in);
 		PrintWriter out = new PrintWriter(new File("c-big.out"));
 		TreeSet<Long> palin = new TreeSet<Long>();
 		for(int i = 1; i <= 10000000; i++)
 			if(palin(i)) palin.add((long)i);
 		
 		TreeSet<Long> fs = new TreeSet<Long>();
 		for(Long x:palin)
 			if(palin(x*x)) fs.add(x*x);
 		
 		Long[] f = fs.toArray(new Long[0]);
 		
 		int times = reader.nextInt();
 		for(int t = 1; t <= times; t++){
 			long a = reader.nextLong();
 			long b = reader.nextLong();
 			
 			int cnt = 0;
 			for(int i = 0; i < f.length; i++)
 				if(f[i] >= a && f[i] <= b)
 					cnt++;
 			
 			out.println("Case #"+t+": "+cnt);
 		}
 		
 		out.close();
 	}
 	
 	public static boolean palin(long x){
 		ArrayList<Long> d = new ArrayList<Long>();
 		while(x > 0){
 			d.add(x%10);
 			x/=10;
 		}
 		boolean good = true;
 		for(int i = 0; i < d.size()/2; i++)
 			good &= d.get(i) == d.get(d.size()-1-i);
 		return good;
 	}
 }

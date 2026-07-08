import java.util.*;
 
 class b{
 	static Scanner sin = new Scanner(System.in);
 	public static void main(String[] args){
 		int t = sin.nextInt();
 		for(int i = 0; i < t; i++){
 			System.out.println("Case #"+(i+1)+": "+go());
 		}
 	}
 	
 	static int go(){
 		int d = sin.nextInt();
 		PriorityQueue<Integer> q = new PriorityQueue<Integer>(2*d);
 		for(int i = 0; i < d; i++){
 			q.add(new Integer(-sin.nextInt()));
 		}
 		if(q.isEmpty()) return 0;
 		
 		int ans = -q.peek().intValue();
 		
 		for(int i = 1; i < ans; i++){
 			int test=0;
 			for(Integer ii: q){
 				int curr = -ii.intValue();
 				test += ((curr+i-1)/i)-1;
 			}
 			if(test + i < ans) ans = test+i;
 		}
 		return ans;
 	}
 }
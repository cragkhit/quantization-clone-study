import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 // Jai Mata Di
 public class GCJ20161ABRankAndFile {
 	public static Scanner console;
 	
 	public static void main(String[] args) {
 		console = new Scanner(System.in);	
 		int noOfTestCases = console.nextInt();
 //		System.out.println("noOfTestCases="+noOfTestCases);
 		for(int testCaseNo = 1; testCaseNo <= noOfTestCases;testCaseNo++){
 			Grid grid = new Grid();
 			grid.input();
 			System.out.print("Case #"+testCaseNo+": ");
 			grid.printMissingList();			
 		}
 	}
 }
 
 class Grid {
 	int n;
 //	List<List<Integer>> grid;
 //	List<List<Integer>> rows;
 //	List<List<Integer>> columns;
 
 	//Queue<List<Integer>> queue;
 	Map<Integer,Integer> count;
 //	Scanner console;
 
 	public Grid() {
 		super();
 		count = new HashMap();
 //		grid = new ArrayList<>();
 //		rows = new ArrayList<>();
 //		columns = new ArrayList<>();
 		
 //		queue = new LinkedList<>();
 		
 	}
 
 	public void input() {
 		n = GCJ20161ABRankAndFile.console.nextInt();
 //		System.out.println("n="+n);
 		GCJ20161ABRankAndFile.console.nextLine();
 		for (int i = 0; i < (2 * n -1); i++) {
 			String numbers = GCJ20161ABRankAndFile.console.nextLine();
 //			List<Integer> nums = new ArrayList<>();
 			Scanner sc = new Scanner(numbers);
 			while (sc.hasNextInt()) {
 				int toBeAdded = sc.nextInt();
 //				nums.add(sc.nextInt());
 				Integer cnt = count.get(toBeAdded);
 				if(cnt == null){
 					count.put(toBeAdded, 1);
 				}else{
 					count.put(toBeAdded, cnt+1);
 				}
 			}
 //			queue.add(nums);
 			sc.close();
 		}
 	}
 	public void printMissingList(){
 		List<Integer> ans = new ArrayList<>();
 		for(Map.Entry<Integer, Integer> e : count.entrySet()){
 			if(e.getValue() % 2 == 1){
 				ans.add(e.getKey());
 			}
 		}
 		Collections.sort(ans);
 		for(Integer i:ans){
 			System.out.print(i+" ");
 		}
 		System.out.println();
 	}
 
 //	public void output(){
 //		List<Integer> l = queue.poll();
 //		while(l != null){
 //			for(Integer i : l){
 //				System.out.print(i + " ");
 //			}
 //			System.out.println();
 //			l = queue.poll();
 //		}
 //	}
 //	public void findMissingList(){
 //		List<Integer> l = queue.poll();
 //		rows.add(l);
 //		l = queue.poll();
 //		while(l != null){
 //			for(Integer i : l){
 //				System.out.print(i + " ");
 //			}
 //			System.out.println();
 //
 //			queue.add(l);
 //			l = queue.poll();
 //		}
 //	}
 }

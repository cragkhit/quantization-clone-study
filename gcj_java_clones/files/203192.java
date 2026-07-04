/*
  * @author Mingxuan Zha
  * @date April 2016, created
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 
 import javax.swing.JFileChooser;
 
 
 public class R2ProblemA {
 	public static void main (String[] args)throws Exception{
 		JFileChooser ourChooser = new JFileChooser(".");
 		int retval = ourChooser.showOpenDialog(null);
 	    if (retval == JFileChooser.APPROVE_OPTION) {
 	        File file = ourChooser.getSelectedFile();
 	        Scanner input = new Scanner(file);
 	        int testSize = Integer.parseInt(input.nextLine());
 	        for(int i=0; i<testSize; i++){
 	        	String s = input.nextLine();
 	        	System.out.println("Case #"+(i+1)+": "+getResult(s));
 	        }
 	    }
 	}
 	
 	public static String getResult(String s){
 		HashMap<Character, Integer> map = new HashMap<>();
 		ArrayList<Integer> numbers = new ArrayList<>();
 		for(int i=0; i<s.length(); i++){
 			if(map.containsKey(s.charAt(i))){
 				int val = map.get(s.charAt(i));
 				map.put(s.charAt(i), val+1);
 			}
 			else{
 				map.put(s.charAt(i), 1);
 			}
 		}
 		
 		//get 0
 		if(map.containsKey('Z') && map.containsKey('E') &&map.containsKey('R') && map.containsKey('O')){
 			int zeros = map.get('Z');
 			for(int i=0; i<zeros; i++){
 				numbers.add(0);
 			}
 			int val1 = map.get('Z');
 			map.put('Z', val1-zeros);
 			int val2 = map.get('E');
 			map.put('E', val2-zeros);
 			int val3 = map.get('R');
 			map.put('R', val3-zeros);
 			int val4 = map.get('O');
 			map.put('O', val4-zeros);
 		}
 		
 		//get 2
 		if(map.containsKey('T') && map.containsKey('W') && map.containsKey('O')){
 			int cur = map.get('W');
 			for(int i=0; i<cur; i++){
 				numbers.add(2);
 			}
 			int val1 = map.get('T');
 			map.put('T', val1-cur);
 			int val2 = map.get('W');
 			map.put('W', val2-cur);
 			int val3 = map.get('O');
 			map.put('O', val3-cur);
 		}
 		
 		//get 4
 		if(map.containsKey('F') && map.containsKey('O') && map.containsKey('U') && map.containsKey('R')){
 			int cur = map.get('U');
 			for(int i=0; i<cur; i++){
 				numbers.add(4);
 			}
 			int val1 = map.get('F');
 			map.put('F', val1-cur);
 			int val2 = map.get('O');
 			map.put('O', val2-cur);
 			int val3 = map.get('U');
 			map.put('U', val3-cur);
 			int val4 = map.get('R');
 			map.put('R', val4-cur);
 		}
 		
 		//get 6
 		if(map.containsKey('S') && map.containsKey('I') && map.containsKey('X')){
 			int cur = map.get('X');
 			for(int i=0; i<cur; i++){
 				numbers.add(6);
 			}
 			int val1 = map.get('S');
 			map.put('S', val1-cur);
 			int val2 = map.get('I');
 			map.put('I', val2-cur);
 			int val3 = map.get('X');
 			map.put('X', val3-cur);
 		}
 		
 		//get 8
 		if(map.containsKey('E') && map.containsKey('I') && map.containsKey('G') && map.containsKey('H') && map.containsKey('T')){
 			int cur = map.get('G');
 			for(int i=0; i<cur; i++){
 				numbers.add(8);
 			}
 			int val1 = map.get('E');
 			map.put('E', val1-cur);
 			int val2 = map.get('I');
 			map.put('I', val2-cur);
 			int val3 = map.get('G');
 			map.put('G', val3-cur);
 			int val4 = map.get('H');
 			map.put('H', val4-cur);
 			int val5 = map.get('T');
 			map.put('T', val5-cur);
 		}
 		
 		//get 5
 		if(map.containsKey('F') && map.containsKey('I') && map.containsKey('V') && map.containsKey('E')){
 			int cur = map.get('F');
 			for(int i=0; i<cur; i++){
 				numbers.add(5);
 			}
 			int val1 = map.get('F');
 			map.put('F', val1-cur);
 			int val2 = map.get('I');
 			map.put('I', val2-cur);
 			int val3 = map.get('V');
 			map.put('V', val3-cur);
 			int val4 = map.get('E');
 			map.put('E', val4-cur);
 		}
 		
 		//get 7
 		if(map.containsKey('S') && map.containsKey('E') && map.containsKey('V') && map.containsKey('E') && map.containsKey('N')){
 			int cur = map.get('V');
 			for(int i=0; i<cur; i++){
 				numbers.add(7);
 			}
 			int val1 = map.get('S');
 			map.put('S', val1-cur);
 			int val2 = map.get('E');
 			map.put('E', val2-cur*2);
 			int val3 = map.get('V');
 			map.put('V', val3-cur);
 			int val5 = map.get('N');
 			map.put('N', val5-cur);
 		}
 		
 		//get 1
 		if(map.containsKey('O') && map.containsKey('N') && map.containsKey('E')){
 			int cur = map.get('O');
 			for(int i=0; i<cur; i++){
 				numbers.add(1);
 			}
 			int val1 = map.get('O');
 			map.put('O', val1-cur);
 			int val2 = map.get('N');
 			map.put('N', val2-cur);
 			int val3 = map.get('E');
 			map.put('E', val3-cur);
 		}
 		
 		//get 3
 		if(map.containsKey('T') && map.containsKey('H') && map.containsKey('R') && map.containsKey('E')){
 			int cur = map.get('R');
 			for(int i=0; i<cur; i++){
 				numbers.add(3);
 			}
 			int val1 = map.get('T');
 			map.put('T', val1-cur);
 			int val2 = map.get('H');
 			map.put('H', val2-cur);
 			int val3 = map.get('R');
 			map.put('R', val3-cur);
 			int val4 = map.get('E');
 			map.put('E', val4-cur*2);
 		}
 		
 		//get 9
 		if(map.containsKey('N') && map.containsKey('I') && map.containsKey('E')){
 			int cur = map.get('I');
 			for(int i=0; i<cur; i++){
 				numbers.add(9);
 			}
 			int val1 = map.get('N');
 			map.put('N', val1-cur*2);
 			int val2 = map.get('I');
 			map.put('I', val2-cur);
 			int val3 = map.get('E');
 			map.put('E', val3-cur);
 		}
 		
 		int[] result = new int[numbers.size()];
 		for(int i=0; i<numbers.size(); i++){
 			result[i] = numbers.get(i);
 		}
 		Arrays.sort(result);
 		String phoneNum = "";
 		for(int i=0; i<result.length; i++){
 			phoneNum = phoneNum + String.valueOf(result[i]);
 		}
 		return phoneNum;
 	}
 }

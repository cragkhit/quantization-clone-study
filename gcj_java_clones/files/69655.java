import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Dijkstra {
 
 	public static void main(String[] args) throws IOException {
 		int i;
 		int l;
 		long x;
 		int T;
 
 		int[][] M = new int[][] { { 0, 0, 0, 0, 0 }, { 0, 1, 2, 3, 4 },
 				{ 0, 2, -1, 4, -3 }, { 0, 3, -4, -1, 2 }, { 0, 4, 3, -2, -1 } };
 
 		BufferedReader br = new BufferedReader(new FileReader("dijkstra.in"));
 		BufferedWriter bw = new BufferedWriter(new FileWriter("dijkstra.out"));
 
 		T = Integer.parseInt(br.readLine());
 		int[] V = new int[] { 2, 3, 4 };
 
 		for (i = 0; i < T; i++) {
 
 			Set<Integer> values = new HashSet<>();
 			int L;
 			long X;
 			String line = br.readLine();
 			String[] lineSplit = line.split(" ");
 			L = Integer.parseInt(lineSplit[0]);
 			X = Long.parseLong(lineSplit[1]);
 			String buf = br.readLine();
 
 			int current = 1;
 			int index = 0;
 			int sign = 1;
 			
 			for (x = 0; x < X; x++) {
 				for (l = 0; l < L; l++) {
 					char c = buf.charAt(l);
 					int val = 0;
 					switch (c) {
 					case '1':
 						val = 1;
 						break;
 					case 'i':
 						val = 2;
 						break;
 					case 'j':
 						val = 3;
 						break;
 					case 'k':
 						val = 4;
 						break;
 					default:
 						break;
 					}
 
 					current = M[current * sign][val] * sign;
 
 					if (current < 0)
 						sign = -1;
 					else
 						sign = 1;
 					if (index < 2) {
 						if (current == V[index]) {
 							current = 1;
 							index++;
 							values.clear();
 						}
 					}
 				}
 				if (values.contains(current))
 					break;
 				values.add(current);
 			}
 			if (index == 2 && sign == 1 && V[index] == current) {
 				bw.write("Case #" + (i + 1) + ": YES\n");
 			} else {
 				bw.write("Case #" + (i + 1) + ": NO\n");
 			}
 		}
 		br.close();
 		bw.close();
 	}
 }
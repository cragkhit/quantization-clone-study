import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class RankFile {
 
 	public static String compute(List<String> input)  {
 		String result = "";
 		Set<String> track = new HashSet<String>();
 		
 		for (int i = 0; i < input.size(); i++)  {
 			String line = input.get(i);
 			String[] item = line.split(" ");
 			for (int j = 0; j < item.length; j++)  {
 				if (track.contains(item[j]))  {
 					track.remove(item[j]);
 				}
 				else  {
 					track.add(item[j]);
 				}
 			}
 		}
 		
 		List<Integer> a = new ArrayList<Integer>();
 		
 		Iterator<String> trackIt = track.iterator();
 		while (trackIt.hasNext())  {
 			a.add(new Integer(trackIt.next()));
 		}
 		
 		Collections.sort(a);
 		for(Integer counter: a) {
 			result = result + counter + " ";
 		}
 		
 		return result.substring(0, result.length() - 1);
 	}
 	
 	public static void main(String[] args)  {
 		try {
 			
 			PrintWriter writer = new PrintWriter("C:/Users/lcheeme1/Downloads/B-large.out");
 			int i = 0;
 
 			int numCases = 0;
 			int caseNum = 0;
 			int nextIndex = 1;
 			int N = 0;
 			List<String> myInput = new ArrayList<String>();
 			
 			for (String line : Files.readAllLines(Paths.get("C:/Users/lcheeme1/Downloads/B-large.in"))) {
 				if (i == 0)  {
 					numCases = (new Integer(line)).intValue();
 				}
 				else {
 					if (i == nextIndex)  {
 						N = (new Integer(line)).intValue();
 						caseNum++;
 						myInput.clear();
 					}
 					else if (i < nextIndex + 2 * N) {
 						myInput.add(line);
 						if (myInput.size() == (2 * N - 1))  {
 							writer.println("Case #" + caseNum + ": " + compute(myInput));
 							nextIndex = i + 1;
 						}
 					}
 				}
 				i++;
 			}
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}
 }
 

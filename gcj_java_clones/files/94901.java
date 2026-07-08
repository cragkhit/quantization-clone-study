import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Program {
 
 	private int numCases = 0;
 	private File testCases;
 	private File outputCases;
 			
 	public Program(File testCases, File outputFile) {
 		this.testCases = testCases;
 		this.outputCases = outputFile;
 	}
 		
 	public void execute() throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(testCases));
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outputCases));
 		
 		List<BigInteger> list = new ArrayList<BigInteger>();
 		
 		// get num of test cases
 		String thisLine = reader.readLine();
 		numCases = Integer.parseInt(thisLine);
 				
 		BigInteger min = new BigInteger("1");
 		BigInteger max = new BigInteger("1000000000000000");
 		
 		int numSuccess = 0;
 		for (BigInteger bi = new BigInteger("1");
 				bi.compareTo(max) < 0;
 				bi = bi.add(BigInteger.ONE)) {
 			
 			String s1 = bi.toString();
 			String s2 = new StringBuffer(s1).reverse().toString();
 			if (!s1.equals(s2)) {
 				continue;
 			}
 			
 			BigInteger sq = bi.pow(2);
 			String sq1 = sq.toString();
 			String sq2 = new StringBuffer(sq1).reverse().toString();
 			
 			if (!sq1.equals(sq2)) {						
 				continue;
 			}
 			
 			if (sq.compareTo(max) <= 0 && sq.compareTo(min) >= 0) {
 				list.add(sq);
 			}
 			
 			if (sq.compareTo(max) > 0) {
 				break;
 			}
 		}
 		
 		for (int i = 0; i < numCases; i++) {
 			thisLine = reader.readLine();
 			min = new BigInteger(thisLine.split(" ")[0]);
 			max = new BigInteger(thisLine.split(" ")[1]);
 			
 			numSuccess = 0;
 			for (BigInteger bigInteger : list) {
 				if (bigInteger.compareTo(max) <= 0 && bigInteger.compareTo(min) >= 0) {
 					numSuccess++;
 				}
 			}
 			
 			writer.write("Case #"+(i+1)+": " + numSuccess + "\n");
 		}
 		
 		reader.close();
 		writer.close();
 	}
 				
 	public static void main(String[] args) {		
 		File input = new File("C-large-1.in");
 		File output = new File("C-large-1.out");
 		Program program = new Program(input, output);
 		try {
 			program.execute();
 		} catch (FileNotFoundException e) {
 			System.err.println("Error reading file. File not found!");
 		} catch (IOException e) {
 			System.err.println("Error reading file line!");
 		}
 	}
 	
 }

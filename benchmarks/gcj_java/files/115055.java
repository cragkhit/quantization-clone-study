package mushroom;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Solver {
 	private FileReader m_fileReader;
 	private FileWriter m_fileWriter;
 	private BufferedReader m_bufferedReader;
 	private BufferedWriter m_bufferedWriter;
 	
 	private int m_numCases;
 	
 	public Solver() {
 		try {
 			m_fileReader = new FileReader("input.txt");
 			m_fileWriter = new FileWriter("output.txt");
 			m_bufferedReader = new BufferedReader(m_fileReader);
 			m_bufferedWriter = new BufferedWriter(m_fileWriter);
 			
 			m_numCases = Integer.parseInt(m_bufferedReader.readLine());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		for(int x = 1; x <= m_numCases; x++) {
 			_solveCase(x);
 		}
 		
 		try {
 			m_bufferedWriter.flush();
 			m_bufferedReader.close();
 			m_bufferedWriter.close();
 			m_fileReader.close();
 			m_fileWriter.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void _solveCase(int caseNum) {
 		int[] mushrooms = null;
 		
 		try {
 			int intervals = Integer.parseInt(m_bufferedReader.readLine());
 			
 			String line = m_bufferedReader.readLine();
 			String[] parts = line.split(" ");
 			
 			mushrooms = new int[parts.length];
 			for(int x = 0; x < parts.length; x++) {
 				mushrooms[x] = Integer.parseInt(parts[x]);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		int eatAny = 0;
 		int eatConst = 0;
 		int maxRate = 0;
 		
 		for(int x = 1; x < mushrooms.length; x++) {
 			int diff = mushrooms[x - 1] - mushrooms[x];
 			
 			if(diff > 0) eatAny += diff;
 			if(diff > maxRate) maxRate = diff;
 		}
 		
 		for(int x = 0; x < mushrooms.length - 1; x++) {
 			if(mushrooms[x] >= maxRate) eatConst += maxRate;
 			else eatConst += mushrooms[x];
 		}
 		
 		try {
 			m_bufferedWriter.write("Case #" + caseNum + ": " + eatAny + " " + eatConst + "\n");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }

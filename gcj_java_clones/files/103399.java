import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class KnapSack {
 	static int capacity;
 	static int numOfItems;
 	private static ArrayList<Board> nodesArray = new ArrayList<Board>();
 
 	private static int[][] opt;
 
 	private static class Board {
 		public int mId;
 		public ArrayList<Character> mData = new ArrayList<Character>(16);
 		public Status status;
 
 		public Board(int i, char[] data) {
 			mId = i;
 			mData = new ArrayList<Character>();
 			for (char ch : data) {
 				mData.add(ch);
 			}
 		}
 
 		public void getOutput() {
 			// Rows
 			for (int i = 0; i < 4; i++) {
 				String thisRow = getStringRepresentation((List<Character>) mData
 						.subList(0 + i * 4, 4 + i * 4));
 				if (thisRow.equals("XXXX") || thisRow.equals("TXXX")
 						|| thisRow.equals("XTXX") || thisRow.equals("XXTX")
 						|| thisRow.equals("XXXT")) {
 					status = Status.XWins;
 					return;
 				} else if (thisRow.equals("OOOO") || thisRow.equals("TOOO")
 						|| thisRow.equals("OTOO") || thisRow.equals("OOTO")
 						|| thisRow.equals("OOOT")) {
 					status = Status.OWins;
 					return;
 				}
 			}
 			// Columns
 			for (int i = 0; i < 4; i++) {
 				String thisColumn = new String();
 				for (int j = 0; j < 4; j++) {
 					thisColumn += mData.get(4 * j + i);
 				}
 				if (thisColumn.equals("XXXX") || thisColumn.equals("TXXX")
 						|| thisColumn.equals("XTXX")
 						|| thisColumn.equals("XXTX")
 						|| thisColumn.equals("XXXT")) {
 					status = Status.XWins;
 					return;
 				} else if (thisColumn.equals("OOOO") || thisColumn.equals("TOOO")
 						|| thisColumn.equals("OTOO") || thisColumn.equals("OOTO")
 						|| thisColumn.equals("OOOT")) {
 					status = Status.OWins;
 					return;
 				}
 			}
 			
 			//Right Diagonal
 			String rightDiagonal = new String();
 			rightDiagonal += mData.get(0);
 			rightDiagonal += mData.get(5);
 			rightDiagonal += mData.get(10);
 			rightDiagonal += mData.get(15);
 			if (rightDiagonal.equals("XXXX") || rightDiagonal.equals("TXXX")
 					|| rightDiagonal.equals("XTXX")
 					|| rightDiagonal.equals("XXTX")
 					|| rightDiagonal.equals("XXXT")) {
 				status = Status.XWins;
 				return;
 			}
 			else if (rightDiagonal.equals("OOOO") || rightDiagonal.equals("TOOO")
 						|| rightDiagonal.equals("OTOO") || rightDiagonal.equals("OOTO")
 						|| rightDiagonal.equals("OOOT"))
 			{
 				status = Status.OWins;
 				return;
 			}
 			
 			//Left Diagonal
 			String leftDiagonal = new String();
 			leftDiagonal += mData.get(3);
 			leftDiagonal += mData.get(6);
 			leftDiagonal += mData.get(9);
 			leftDiagonal += mData.get(12);
 			if (leftDiagonal.equals("XXXX") || leftDiagonal.equals("TXXX")
 					|| leftDiagonal.equals("XTXX")
 					|| leftDiagonal.equals("XXTX")
 					|| leftDiagonal.equals("XXXT")) {
 				status = Status.XWins;
 				return;
 			}
 			else if (leftDiagonal.equals("OOOO") || leftDiagonal.equals("TOOO")
 						|| leftDiagonal.equals("OTOO") || leftDiagonal.equals("OOTO")
 						|| leftDiagonal.equals("OOOT"))
 			{
 				status = Status.OWins;
 				return;
 			}
 			
 			
 			
 			if(getStringRepresentation(mData).contains("."))
 			{
 				status = Status.NotCompleted;
 			}
 			else
 			{
 				status = Status.Draw;
 			}
 		}
 
 		public enum Status {
 			XWins, OWins, Draw, NotCompleted;
 			
 			public String getStatusString()
 			{
 				if(this.equals(XWins))
 				{
 					return "X won";
 				}
 				else if(this.equals(OWins))
 				{
 					return "O won";
 				}
 				else if(this.equals(Draw))
 				{
 					return "Draw";
 				}
 				else
 				{
 					return "Game has not completed";
 				}
 			}
 		}
 	}
 
 	public static String getStringRepresentation(List<Character> list) {
 		StringBuilder builder = new StringBuilder(list.size());
 		for (Character ch : list) {
 			builder.append(ch);
 		}
 		return builder.toString();
 	}
 
 	public static void main(String[] args) throws NumberFormatException,
 			IOException {
 
 		FileInputStream fstream = new FileInputStream(
 				System.getProperty("user.dir") + "/test1.txt");
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String strLine;
 		int nodesCount = 0;
 		char[] temp = new char[16];
 		int boardCount = 0;
 		// Read File Line By Line
 		while ((strLine = br.readLine()) != null) {
 			String[] values;
 			if (numOfItems == 0) {
 				numOfItems = Integer.valueOf(strLine);
 			} else {
 				if (boardCount==4) {
 					Board b = new Board(nodesCount, temp);
 					b.getOutput();
 					nodesArray.add(b);
 					nodesCount++;
 					boardCount = 0;
 					temp = new char[16];
 				} else {
 					strLine.getChars(0, 4, temp, 0 + boardCount * 4);
 					boardCount++;
 				}
 
 			}
 		}
 		Board bb = new Board(nodesCount, temp);
 		bb.getOutput();
 		nodesArray.add(bb);
 		nodesCount++;
 		for (Board b : nodesArray) {
 			System.out.println("Case #"+(b.mId+1)+": "+ b.status.getStatusString());
 		}
 	}
 
 }

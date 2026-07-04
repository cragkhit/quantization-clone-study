import java.io.*;
 import java.util.*;
 
 public class Game {
 	public static final int EMPTY = 0;
 	public static final int O = 1;
 	public static final int X = 2;
 	public static final int T = 3;
 	public static final int DRAW = 4;
 	public static final int INCOMPLETE = -1;
 
 	public static int getPeiceFromChar(char peice) {
 		switch (peice) {
 			case 'X':
 				return Game.X;
 			case 'O':
 				return Game.O;
 			case 'T':
 				return Game.T;
 			case '.':
 				return Game.EMPTY;
 		}
 		return -2;
 	}
 	
 	private int[][] board;
 
 	public Game(String board) {
 		this.board = new int[4][4];
 		
 		Scanner s = new Scanner(board);
 		s.useDelimiter("");
 		for (int y=0; y<4; y++) {
 			for (int x=0; x<4; x++) {
 				this.board[y][x] = Game.getPeiceFromChar(s.next().charAt(0));
 			}
 			//System.out.println(s.next());
 		}
 	}
 	
 	public int winner() {
 		boolean contains_emptys = false;
 		int winner = -2;
 		for (int y=0; y<4; y++) {
 			int last = Game.T;
 			boolean found_winner = true;
 			for (int x=0; x<4; x++) {
 				if (this.board[y][x] == last || last == Game.T || this.board[y][x] == Game.T && this.board[y][x] != Game.EMPTY) {
 					if (this.board[y][x] != Game.T) last = this.board[y][x];
 				} else {
 					found_winner = false;
 					if (this.board[y][x] == Game.EMPTY) contains_emptys = true;
 				}
 			}
 			if (found_winner) return last;
 		}
 		
 		for (int x=0; x<4; x++) {
 			int last = Game.T;
 			boolean found_winner = true;
 			for (int y=0; y<4; y++) {
 				if (this.board[y][x] == last || last == Game.T || this.board[y][x] == Game.T && this.board[y][x] != Game.EMPTY) {
 					if (this.board[y][x] != Game.T) last = this.board[y][x];
 				} else {
 					found_winner = false;
 					if (this.board[y][x] == Game.EMPTY) contains_emptys = true;
 				}
 			}
 			if (found_winner) return last;
 		}
 		
 		{
 			int last = Game.T;
 			boolean found_winner = true;
 			for (int k=0; k<4; k++) {
 				if (this.board[k][k] == last || last == Game.T || this.board[k][k] == Game.T && this.board[k][k] != Game.EMPTY) {
 					if (this.board[k][k] != Game.T) last = this.board[k][k];
 				} else {
 					found_winner = false;
 					if (this.board[k][k] == Game.EMPTY) contains_emptys = true;
 				}
 			}
 			if (found_winner) return last;
 		}
 		
 		{
 			int last = Game.T;
 			boolean found_winner = true;
 			for (int k=0; k<4; k++) {
 				if (this.board[3-k][k] == last || last == Game.T || this.board[3-k][k] == Game.T && this.board[3-k][k] != Game.EMPTY) {
 					if (this.board[3-k][k] != Game.T) last = this.board[3-k][k];
 				} else {
 					found_winner = false;
 					if (this.board[3-k][k] == Game.EMPTY) contains_emptys = true;
 				}
 			}
 			if (found_winner) return last;
 		}
 		
 		if (contains_emptys) return Game.INCOMPLETE;
 		return Game.DRAW;
 	}
 	
 	public static void main(String[] args) throws IOException {
 		String file = "A-large";
 		Scanner in = new Scanner(new File(file + ".in"));
 		PrintStream out = new PrintStream(new File(file + ".out"));
 		
 		int cases = Integer.parseInt(in.nextLine());
 		for (int c = 1; c <= cases; c++) {
 			String board = "";
 			board += in.nextLine();
 			board += in.nextLine();
 			board += in.nextLine();
 			board += in.nextLine();
 			Game g = new Game(board);
 			
 			switch (g.winner()) {
 				case Game.X:
 					out.println("Case #" + c + ": X won");
 					break;
 				case Game.O:
 					out.println("Case #" + c + ": O won");
 					break;
 				case Game.DRAW:
 					out.println("Case #" + c + ": Draw");
 					break;
 				case Game.INCOMPLETE:
 					out.println("Case #" + c + ": Game has not completed");
 					break;
 				default:
 					out.println("Case #" + c + ": Game has not completed");
 			}
 			in.nextLine(); // skip the whitespace
 		}
 	}
 }
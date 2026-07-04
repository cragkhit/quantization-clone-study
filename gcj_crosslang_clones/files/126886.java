import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 public class TicTacToeTomek {
 
 	public static void main(String[] args) throws NumberFormatException, IOException {
 		BufferedReader br = new BufferedReader(new FileReader("A-2013"));
 
 		String [] board = new String[4];
 		
 		int cases = Integer.parseInt(br.readLine());
 		int caseNum = 0;
 		
 		while(cases-- > 0){
 			caseNum++;
 			
 			for(int i = 0; i < 4; i++)
 				board[i] = br.readLine();
 			
 			if (caseNum != 0) {
 				br.readLine();
 			}
 			
 			int emptyCells = 0;
 			boolean xWon = false, oWon = false;
 			
 			for (int i = 0; i < board.length && !xWon && !oWon; i++) {
 				boolean temp = true;
 				char first;
 				int j;
 				
 				if (board[i].charAt(0) == 'T') {
 					first = board[i].charAt(1);
 					j = 2;
 				}
 				else {
 					if (board[i].charAt(0) == '.') {
 						emptyCells++;
 					}
 					first = board[i].charAt(0);
 					j = 1;
 				}
 				
 				for (; j < board[i].length() && !xWon && !oWon; j++) {
 					if (board[i].charAt(j) == '.') {
 						emptyCells++;
 						temp = false;
 					}
 					
 					if (temp && board[i].charAt(j) != '.' && (board[i].charAt(j) == first || board[i].charAt(j) == 'T')) {
 						
 					}
 					else if (temp){
 						temp = false;
 						
 					}
 				}
 				if (temp) {
 					switch(first){
 					case 'X':
 						xWon = true;
 						break;
 					case 'O':
 						oWon = true;
 						break;
 					}
 				}
 			}
 			
 			for (int i = 0; i < board.length && !xWon && !oWon; i++) {
 				boolean temp = true;
 				char first;
 				int j;
 				
 				if (board[0].charAt(i) == 'T') {
 					first = board[1].charAt(i);
 					j = 2;
 				}
 				else {
 					first = board[0].charAt(i);
 					j = 1;
 				}
 				
 				for (; j < board[i].length() && !xWon && !oWon; j++) {
 					if (temp && board[j].charAt(i) != '.' && (board[j].charAt(i) == first || board[j].charAt(i) == 'T')) {
 						
 					}
 					else if (temp){
 						temp = false;
 						
 					}
 				}
 				if (temp) {
 					switch(first){
 					case 'X':
 						xWon = true;
 						break;
 					case 'O':
 						oWon = true;
 						break;
 					}
 				}
 			}
 			
 			boolean temp = !xWon && !oWon;
 			
 			for (int i = 1; i < board.length && !xWon && !oWon && temp; i++) {
 				char one = board[i - 1].charAt(i - 1), two = board[i].charAt(i);
 				if (one != two && one != 'T' && two != 'T') {
 					temp = false;
 				}
 			}
 			
 			char first = board[0].charAt(0);
 			
 			if (first == 'T') {
 				first = board[1].charAt(1);
 			}
 			
 			if (temp) {
 				switch(first){
 				case 'X':
 					xWon = true;
 					break;
 				case 'O':
 					oWon = true;
 					break;
 				}
 			}
 			
 			temp = !xWon && !oWon;
 			
 			for (int i = 1; i < board.length && !xWon && !oWon && temp; i++) {
 				char one = board[i - 1].charAt(4 - i), two = board[i].charAt(4 - i - 1);
 				if (one != two && one != 'T' && two != 'T') {
 					temp = false;
 				}
 			}
 			
 			first = board[0].charAt(3);
 			
 			if (first == 'T') {
 				first = board[1].charAt(2);
 			}
 			
 			if (temp) {
 				switch(first){
 				case 'X':
 					xWon = true;
 					break;
 				case 'O':
 					oWon = true;
 					break;
 				}
 			}
 			
 			System.out.print("Case #" + caseNum +": ");
 			
 			if (xWon) {
 				System.out.println("X won");
 			}
 			else if(oWon){
 				System.out.println("O won");
 			}
 			else if(emptyCells != 0){
 				System.out.println("Game has not completed");
 			}
 			else {
 				System.out.println("Draw");
 			}
 		}
 
 	}
 
 }

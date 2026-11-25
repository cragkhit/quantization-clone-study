import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class MinesSweeperCode{
 	
 	public static String ifilename = "d:\\codejam\\C-large.in";
 	//public static String ifilename = "d:\\codejam\\C-small-attempt1.in";
 //	public static String ifilename = "d:\\codejam\\input.txt";
 	public static int noOfInputLines = 1;
 	static ArrayList<String> alInput = new ArrayList<String>();
 	public static int NofTestCases = 0;	   
 	static int rowcount = 4;
 	static String outputFile = "d:\\codejam\\output.txt";
 	static int MINES =1;
 	static int NOTMINES = 0;
 	static int MINE_NEIG = 2;
 	static int POSSIBLE_NEIGHBOURS = 3;
 	
 	public static void InputConvertor(String ifilename) 
 	{
 		alInput = inputReader(new File(ifilename));
 			
 	}
 	
 	public static boolean placeInputMines(int inputNumber,InputMines n){
 		
 		boolean placedallMines=true;
 		String altestcase = alInput.get(inputNumber).trim();
 		String al[] = altestcase.split(" ");
 		
 		n.Rows = Integer.parseInt(al[0]);
 		n.Cols = Integer.parseInt(al[1]);
 		n.Mines = Integer.parseInt(al[2]);		 
 		n.arr = new int[n.Rows][n.Cols];
 		n.arrpossible = new int[n.Rows][n.Cols];
 		
 		if(n.Mines == 0 ){
 			return true;	
 		}
 		
 		n.leftPlaces = (n.Rows * n.Cols)-n.Mines; 
 		if( n.leftPlaces == 1 ){
 			for(int i=0;i<n.Rows;i++){
 				for(int j=0;j < n.Cols;j++){
 					n.arr[i][j]=MINES;
 				}
 			}
 			n.arr[0][0]=NOTMINES;
 			return true;
 		}
 		
 		while((n.Mines >= n.Cols - n.mincol || n.Mines >= n.Rows - n.minrow) && n.mincol+2 < n.Cols && n.minrow +2 < n.Rows){
 			
 			if(n.Cols <= n.Rows){
 			if(n.Mines >= n.Cols - n.mincol){
 				for(int i=n.mincol;i<n.Cols;i++){
 					n.arr[n.minrow][i]=MINES;
 					n.Mines--;				
 				}
 				
 				if(n.minrow+1 < n.Rows)
 					 n.minrow++;
 			}
 			
 			if(n.Mines >= n.Rows - n.minrow ){
 				for(int i=n.minrow;i<n.Rows;i++){
 					n.arr[i][n.mincol]=MINES;
 					n.Mines--;			
 					
 				}
 				
 				if(n.mincol+1 < n.Cols)
 					n.mincol++;
 			}
 			}else{
 				
 				if(n.Mines >= n.Rows - n.minrow ){
 					for(int i=n.minrow;i<n.Rows;i++){
 						n.arr[i][n.mincol]=MINES;
 						n.Mines--;			
 						
 					}
 					
 					if(n.mincol+1 < n.Cols)
 						n.mincol++;
 				}
 				if(n.Mines >= n.Cols - n.mincol){
 					for(int i=n.mincol;i<n.Cols;i++){
 						n.arr[n.minrow][i]=MINES;
 						n.Mines--;				
 					}
 					
 					if(n.minrow+1 < n.Rows)
 						 n.minrow++;
 				}
 
 				
 			}
 			
 			
 			}
 		
 		markNeighbours(n);
 		//printMatrix(n);
 		//System.out.println(n.Mines);
 		while(n.Mines > 0){			
 			//System.out.println(n.minrow +" "+n.mincol+" set to mines");
 			
 			n.arr[n.minrow][n.mincol]=MINES;
 			//printMatrix(n);
 			n.Mines--;
 			markNeighbours(n);
 			//printMatrix(n);
 			//System.out.print("mines " + n.Mines);
 			if(n.Mines > 0){				
 				selectMineNeighbours(n.minrow,n.mincol,n);				
 				//setminimum and maximum
 				if(!n.isPlacetomark){
 				//System.out.println("NO place for mines");
 					return false;
 				}
 			}
 			
 			markNeighbours(n);
 			//printMatrix(n);
 		}
 
 	 return placedallMines;
 	}
 
 	private static void markpossibleN(int rowv,int colv, InputMines n) {
 		// TODO Auto-generated method stub
 		// TODO Auto-generated method stub
 		copyArray(n.arrpossible,n.arr,n);
 		n.arrpossible[0][0]=7;
 		//printMatrix(n);
 		if(rowv -1 >= 0 && colv -1 >= 0)			
 			if(n.arr[rowv-1][colv-1] == NOTMINES  )
 				n.arr[rowv-1][colv-1]=MINE_NEIG;
 
 		
 		if(rowv -1 >= 0)
 			if(n.arr[rowv-1][colv] == NOTMINES )
 				n.arr[rowv-1][colv] =MINE_NEIG;
 				
 		if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 			if(n.arr[rowv-1][colv+1] == NOTMINES  )
 				n.arr[rowv-1][colv+1]=MINE_NEIG;
 		
 		
 		if(colv -1 >= 0)
 			if(n.arr[rowv][colv-1] == NOTMINES  )
 				n.arr[rowv][colv-1] =MINE_NEIG;
 		
 		if(rowv +1 < n.Rows && colv -1 >= 0)
 			if(n.arr[rowv+1][colv-1] == NOTMINES  )
 				n.arr[rowv+1][colv-1]=MINE_NEIG;
 		
 		if(colv +1 < n.Cols)
 			if(n.arr[rowv][colv+1] == NOTMINES  )
 				n.arr[rowv][colv+1]=MINE_NEIG;
 		
 		if(rowv +1 < n.Rows)
 			if(n.arr[rowv+1][colv] == NOTMINES )
 				n.arr[rowv][colv+1]=MINE_NEIG;
 		
 		if(rowv +1 < n.Rows && colv +1 < n.Cols)
 			if(n.arr[rowv+1][colv+1] == NOTMINES  )
 				n.arr[rowv][colv+1]=MINE_NEIG;
 		
 		}
 		
 
 	private static void markNeighbours(InputMines n) {
 		// TODO Auto-generated method stub
 		
 		for(int i1 =0 ; i1 < n.Rows ;i1++){
 			for(int j1=0;j1<n.Cols;j1++){
 				if(n.arr[i1][j1] == MINES){
 					markNonMinesN(i1, j1, n.arr, n);
 				}
 				
 			}
 		}
 
 		
 	}
 
 	private static void copyArray(int[][] arrpossible, int[][] arr,InputMines n) {
 		// TODO Auto-generated method stub
 		
 		for(int i1 =0 ; i1 < n.Rows ;i1++){
 
 			for(int j1=0;j1<n.Cols;j1++){
 				arrpossible[i1][j1] = arr[i1][j1];
 				
 			}
 		}
 
 		
 	}
 
 	private static void markNonMinesN(int rowv,int colv, int[][] narr,InputMines n) {
 		// TODO Auto-generated method stub
 		// TODO Auto-generated method stub
 		if(rowv -1 >= 0 && colv -1 >= 0)			
 			if(narr[rowv-1][colv-1] == NOTMINES  )
 				narr[rowv-1][colv-1]=MINE_NEIG;
 
 		
 		if(rowv -1 >= 0)
 			if(narr[rowv-1][colv] == NOTMINES )
 				narr[rowv-1][colv] =MINE_NEIG;
 				
 		if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 			if(narr[rowv-1][colv+1] == NOTMINES  )
 				narr[rowv-1][colv+1]=MINE_NEIG;
 		
 		
 		if(colv -1 >= 0)
 			if(narr[rowv][colv-1] == NOTMINES  )
 				narr[rowv][colv-1] =MINE_NEIG;
 		
 		if(rowv +1 < n.Rows && colv -1 >= 0)
 			if(narr[rowv+1][colv-1] == NOTMINES  )
 				narr[rowv+1][colv-1]=MINE_NEIG;
 		
 		if(colv +1 < n.Cols)
 			if(narr[rowv][colv+1] == NOTMINES  )
 				narr[rowv][colv+1]=MINE_NEIG;
 		
 		if(rowv +1 < n.Rows)
 			if(narr[rowv+1][colv] == NOTMINES )
 				narr[rowv+1][colv]=MINE_NEIG;
 		
 		if(rowv +1 < n.Rows && colv +1 < n.Cols)
 			if(narr[rowv+1][colv+1] == NOTMINES  )
 				narr[rowv+1][colv+1]=MINE_NEIG;
 		//System.out.println("hi");
 		//printMatrix(n);
 		
 		}
 
 	private static void selectMineNeighbours(int n1rowv,int n1colv, InputMines n) {
 		// TODO Auto-generated method stub
 		n.isPlacetomark = false;		
 		int minneighbour = 10;
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 			for(int j1=0;j1<n.Cols;j1++){
 				if(n.arr[i1][j1] == MINE_NEIG){
 					if(isPossibleToSelectAsMine(i1, j1, n)){
 						//System.out.println(" for "+n1rowv +" "+n1colv);
 						//System.out.println(" first possible "+i1 +" "+j1);
 						int c = countNonmineNeighbour(i1, j1, n);
 						//	System.out.println("neighbours"+c);
 						if(c < minneighbour){
 							n.minrow = i1;
 							n.mincol = j1;
 							minneighbour = c;
 							n.isPlacetomark = true;
 						}						
 						
 					}
 						
 				}
 			}
 			
 		}
 		if(n.isPlacetomark){
 			return;
 		}
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 			for(int j1=0;j1<n.Cols;j1++){
 				if(n.arr[i1][j1] == MINE_NEIG){
 					if(isPossibleToSelectAsMineLessMines(i1, j1, n)){
 						//System.out.println(" for "+n1rowv +" "+n1colv);
 						//System.out.println(" first possible "+i1 +" "+j1);
 						int c = countNonmineNeighbour(i1, j1, n);
 						//	System.out.println("neighbours"+c);
 						if(c < minneighbour){
 							n.minrow = i1;
 							n.mincol = j1;
 							minneighbour = c;
 							n.isPlacetomark = true;
 						}						
 						
 					}
 						
 				}
 			}
 			
 		}
 		
 		 //System.out.println("min set to "+n.minrow + " "+n.mincol);
 	}
 		static boolean isPossibleToSelectAsMineLessMines(int i1,int j1, InputMines n){
 			copyArray(n.arrpossible, n.arr, n);
 			boolean zeroMines = true;
 			if(hasZeroNeighbour(i1, j1, n)){
 				n.arrpossible[i1][j1] = MINES;
 			
 				markNonMinesN(i1,j1,n.arrpossible ,n);
 				//System.out.println("This is i am printing possiblity after marking");
 				//printMatrix(n,n.arrpossible);
 				int nonZeroMines=0;
 				
 				for(int ic =0 ; ic < n.Rows;ic++){
 				for(int jc=0;jc<n.Cols;jc++){
 					if(n.arrpossible[ic][jc]== MINE_NEIG)
 					{	
 						if(!hasZeroNeighbour(ic, jc, n,n.arrpossible)){
 							nonZeroMines++;
 							zeroMines = false;
 							n.arrpossible[ic][jc] = 4;
 						}
 						//System.out.println(" i got zero "+i1 +" "+j1);
 					}
 				}			
 			
 				}
 				
 				if(!zeroMines && nonZeroMines < n.Mines){
 					zeroMines = true;
 				}
 
 		}
 						return zeroMines;
 		}
 	
 		static boolean isPossibleToSelectAsMine(int i1,int j1, InputMines n){
 			
 			copyArray(n.arrpossible, n.arr, n);			
 			if(hasZeroNeighbour(i1, j1, n)){
 				n.arrpossible[i1][j1] = MINES;
 			
 				markNonMinesN(i1,j1,n.arrpossible ,n);
 				//System.out.println("This is i am printing possiblity after marking");
 				//printMatrix(n,n.arrpossible);
 				
 				
 				for(int ic =0 ; ic < n.Rows;ic++){
 				for(int jc=0;jc<n.Cols;jc++){
 					if(n.arrpossible[ic][jc]== MINE_NEIG)
 					{	
 						if(!hasZeroNeighbour(ic, jc, n,n.arrpossible)){
 							return false;
 						}
 						//System.out.println(" i got zero "+i1 +" "+j1);
 					}
 				}			
 			
 				}
 				
 
 		}
 						return true;
 		}
 
 	private static int countNonmineNeighbour(int rowv,int colv,InputMines n) {
 
 		int neighbour=0;
 		
 		// TODO Auto-generated method stub
 		if(rowv -1 >= 0)
 			if(n.arr[rowv-1][colv] != MINES )
 				neighbour++;
 
 		if(rowv -1 >= 0 && colv -1 >= 0)			
 			if(n.arr[rowv-1][colv-1] != MINES )
 				neighbour++;
 				
 
 		if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 			if(n.arr[rowv-1][colv+1] != MINES )
 				neighbour++;
 				
 			
 		if(colv -1 >= 0)
 			if(n.arr[rowv][colv-1] != MINES )
 				neighbour++;
 		
 		if(colv +1 < n.Cols)
 			if(n.arr[rowv][colv+1] != MINES )
 				neighbour++;
 		
 		if(rowv +1 < n.Rows && colv -1 >= 0)
 			if(n.arr[rowv+1][colv-1] != MINES )
 				neighbour++;
 		
 		if(rowv +1 < n.Rows)
 			if(n.arr[rowv+1][colv] != MINES )
 				neighbour++;
 				
 		if(rowv +1 < n.Rows && colv +1 < n.Cols)
 			if(n.arr[rowv+1][colv+1] != MINES )
 				neighbour++;
 		
 		//n.nonmineneighv[rowv][colv] = neighbour;
 		return neighbour;
 	}
 	private static int countNonmineNeighbour(int rowv,int colv,InputMines n,int[][] narr) {
 
 
 		int neighbour=0;
 		
 		// TODO Auto-generated method stub
 		if(rowv -1 >= 0)
 			if(narr[rowv-1][colv] != MINES )
 				neighbour++;
 
 		if(rowv -1 >= 0 && colv -1 >= 0)			
 			if(narr[rowv-1][colv-1] != MINES )
 				neighbour++;
 				
 
 		if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 			if(narr[rowv-1][colv+1] != MINES )
 				neighbour++;
 				
 			
 		if(colv -1 >= 0)
 			if(narr[rowv][colv-1] != MINES )
 				neighbour++;
 		
 		if(colv +1 < n.Cols)
 			if(narr[rowv][colv+1] != MINES )
 				neighbour++;
 		
 		if(rowv +1 < n.Rows && colv -1 >= 0)
 			if(narr[rowv+1][colv-1] != MINES )
 				neighbour++;
 		
 		if(rowv +1 < n.Rows)
 			if(narr[rowv+1][colv] != MINES )
 				neighbour++;
 				
 		if(rowv +1 < n.Rows && colv +1 < n.Cols)
 			if(narr[rowv+1][colv+1] != MINES )
 				neighbour++;
 		
 		//n.nonmineneighv[rowv][colv] = neighbour;
 		return neighbour;
 	}
 
 	public static void main(String[] args) {
 		
 		StringBuffer bn = new StringBuffer();
 		boolean possible = true;
 		InputConvertor(ifilename);
 		for(int i =0; i< NofTestCases ; i++){
 			InputMines n = new InputMines();
 			possible = placeInputMines(i,n);
 
 			bn.append("Case #"+(i+1)+":\n");
 			if(possible){
 				
 				if(n.leftPlaces == 1){
 					generatePossibleCase(bn,n);
 				}else{
 				boolean b = allNonZerosRevealed(n);
 				if(b){
 					//System.out.println(b);
 					generatePossibleCase(bn,n);
 				}else{
 					bn.append("Impossible");
 					//System.out.println(i+1);
 					}
 				
 				}
 			}else{
 				bn.append("Impossible");
 				//System.out.println(i+1);
 			}
 			
 
 			if(i < NofTestCases-1){
 				bn.append("\n");
 			}
 		}
 		
 		outputCreator(bn);
 	}
 	
 	
 
 	private static boolean allNonZerosRevealed(InputMines n) {
 		
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 			
 			for(int j1=0;j1<n.Cols;j1++){
 				if(n.arr[i1][j1]== MINE_NEIG){
 					//printMatrix(n);
 					boolean b = hasZeroNeighbour(i1,j1,n);
 					if(!b){
 						return false;
 					}
 				}
 				}
 			}
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	private static boolean generatePossibleCase(StringBuffer bn,InputMines n)
 	{
 		boolean markedc = false;
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 
 			for(int j1=0;j1<n.Cols;j1++){
 				if(n.arr[i1][j1]== MINES){
 					bn.append("*");	
 				}else if(n.arr[i1][j1] == MINE_NEIG){
 					bn.append(".");
 				}else{	
 					if(!markedc){
 					bn.append("c");
 					markedc = true;
 					}else{
 					bn.append(".");
 					}
 				}
 			}
 			if(i1 < n.Rows-1){
 			bn.append("\n");}
 		}
 
 		// TODO Auto-generated method stub
 		return markedc;
 	}
 
 	private static boolean printMatrix(InputMines n)
 	{
 		boolean markedc = false;
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 
 			for(int j1=0;j1<n.Cols;j1++){
 				System.out.print(n.arr[i1][j1]+" ");
 			}
 			System.out.println();
 		}
 
 		// TODO Auto-generated method stub
 		return markedc;
 	}
 	private static boolean printMatrix(InputMines n,int[][] narr)
 	{
 		//System.out.println("arrnpossible");
 		boolean markedc = false;
 		for(int i1 =0 ; i1 < n.Rows;i1++){
 
 			for(int j1=0;j1<n.Cols;j1++){
 				System.out.print(narr[i1][j1]+" ");
 			}
 			System.out.println();
 		}
 
 		// TODO Auto-generated method stub
 		return markedc;
 	}
 
 	public static ArrayList<String> inputReader(File filename)
 	{
 		ArrayList<String> input =null;
 		Scanner scn = null;
 		try {
 			
 			 scn = new Scanner(filename);
 			 NofTestCases = Integer.parseInt(scn.nextLine().trim());
 			 input = new ArrayList<String>();
 
 			 while(scn.hasNextLine()){				 				 
 				 input.add(scn.nextLine());				 
 			 }			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			scn.close();
 			
 		}
 		
 		return input;		
 	}
 
 
 
 public static void outputCreator(StringBuffer bn){
 	BufferedWriter bw = null;
 	
 	try {
 		 
 		File file = new File(outputFile);
 		if(file.exists()){
 			file.delete();
 		}
 		// if file doesnt exists, then create it
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 
 		FileWriter fw = new FileWriter(file.getAbsoluteFile());
 		bw = new BufferedWriter(fw);
 		bw.write(bn.toString());
 		
 
 	} catch (IOException e) {
 		e.printStackTrace();
 	}finally{
 		try {
 			bw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
 
 public static String decimalFormatter(Integer vl){
 	DecimalFormat df = new DecimalFormat("#.#######");
 	return df.format(vl);
 }
 
 public static int markMindSetNeighbour(int rowv,int colv,InputMines n,int minneighbour){
 	
 	if(n.arr[rowv][colv] != MINES ){
 		n.arr[rowv][colv]=MINE_NEIG;
 		int abc = countNonmineNeighbour(rowv,colv,n);
 		
 		if( abc < minneighbour){
 			if(hasZeroNeighbour(rowv,colv,n)){
 			n.minrow = rowv;
 			n.mincol = colv;
 			
 			minneighbour = abc; 
 			n.isPlacetomark = true;
 //			System.out.println("got a 0 neighbour");
 			}
 		}
 	}
 	
 	return minneighbour;
 }
 
 private static boolean hasZeroNeighbour(int rowv, int colv, InputMines n) {
 	boolean nonmineneighbour = false;
 	
 	// TODO Auto-generated method stub
 	if(rowv -1 >= 0 && colv -1 >= 0)			
 		if(n.arr[rowv-1][colv-1] == NOTMINES  )
 			return true;
 
 	
 	if(rowv -1 >= 0)
 		if(n.arr[rowv-1][colv] == NOTMINES )
 			return true;
 			
 	if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 		if(n.arr[rowv-1][colv+1] == NOTMINES  )
 			return true;
 	
 	
 	if(colv -1 >= 0)
 		if(n.arr[rowv][colv-1] == NOTMINES  )
 			return true;
 	
 	if(rowv +1 < n.Rows && colv -1 >= 0)
 		if(n.arr[rowv+1][colv-1] == NOTMINES  )
 			return true;
 	
 	if(colv +1 < n.Cols)
 		if(n.arr[rowv][colv+1] == NOTMINES  )
 			return true;
 	
 	if(rowv +1 < n.Rows)
 		if(n.arr[rowv+1][colv] == NOTMINES )
 			return true;
 	
 	if(rowv +1 < n.Rows && colv +1 < n.Cols)
 		if(n.arr[rowv+1][colv+1] == NOTMINES  )
 			return true;
 	
 	//n.nonmineneighv[rowv][colv] = neighbour;
 	return nonmineneighbour;
 
 }
 
 private static boolean hasZeroNeighbour(int rowv, int colv, InputMines n,int[][] narr) {
 	boolean nonmineneighbour = false;
 	
 	// TODO Auto-generated method stub
 	if(rowv -1 >= 0 && colv -1 >= 0)			
 		if(narr[rowv-1][colv-1] == NOTMINES  )
 			return true;
 
 	
 	if(rowv -1 >= 0)
 		if(narr[rowv-1][colv] == NOTMINES )
 			return true;
 			
 	if(rowv -1 >= 0 &&  colv +1 < n.Cols)				
 		if(narr[rowv-1][colv+1] == NOTMINES  )
 			return true;
 	
 	
 	if(colv -1 >= 0)
 		if(narr[rowv][colv-1] == NOTMINES  )
 			return true;
 	
 	if(rowv +1 < n.Rows && colv -1 >= 0)
 		if(narr[rowv+1][colv-1] == NOTMINES  )
 			return true;
 	
 	if(colv +1 < n.Cols)
 		if(narr[rowv][colv+1] == NOTMINES  )
 			return true;
 	
 	if(rowv +1 < n.Rows)
 		if(narr[rowv+1][colv] == NOTMINES )
 			return true;
 	
 	if(rowv +1 < n.Rows && colv +1 < n.Cols)
 		if(narr[rowv+1][colv+1] == NOTMINES  )
 			return true;
 	
 	//n.nonmineneighv[rowv][colv] = neighbour;
 	return nonmineneighbour;
 
 }
 
 }
 class InputMines{
 	
 	public int Rows = 0;
 	public int Cols = 0;
 	public int Mines = 0;	
 
 	int[][] arr ;
 	int[][] arrpossible ;
 //	int[][] nonmineneighv ;
 	int minrow;
 	int mincol;
 	public int leftPlaces = 0;
 	boolean isPlacetomark = true;
 }
 

import java.util.*;
 
 class Main
 {	
 	public static void main(String[] args)
 	{
 		Scanner in = new Scanner(System.in);
 		int TT = in.nextInt();
 		
 		for(int T = 1; T <= TT; ++T)
 		{
 			String ret = doWork(in);
 			
 			System.out.println("Case #" + T + ": " + ret);
 		}
 	}
 	
 	public static String doWork(Scanner in)
 	{
 		return (richardCanWin(in) ? "RICHARD" : "GABRIEL");
 	}
 	
 	public static boolean richardCanWin(Scanner in)
 	{
 		int X = in.nextInt();
 		int R = in.nextInt();
 		int C = in.nextInt();
 		int MinS = Math.min(R,C);
 		int MaxS = Math.max(R,C);
 		
 		// the area must divide evenly into X or gabriel's task is impossible
 		if((R*C) % X != 0)
 		{
 			return true; 
 		}
 		
 		//lol if this actually works for D-Large I'll laugh
 		//That said... I can't find a counter-example
 		switch(X)
 		{
 			case 1:
 				return false; //Gabriel always wins
 			case 2:
 				return false; //Gabriel always wins for even R*C
 			case 3:
 				return (MinS < 2 || MaxS < 3);
 			case 4:
 				return (MinS < 3 || MaxS < 4);
 			case 5:
 				return (MinS < 3 || MaxS < 5);
 			case 6:
 				return (MinS < 4 || MaxS < 6);
 			default: 
 				//7 or more n-ominos can contain 1x1 holes inside
 				//Gabriel can never fill these
 				return true;
 		}
 	}
 }
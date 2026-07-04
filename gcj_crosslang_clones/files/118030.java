import java.util.Scanner;
 
 public class MushroomMonster {
 
 	int anyMushroomsAtAnyTime(int numbMushrooms, int mushrooms[]){
 		
 		int mushroomsEaten = 0;
 		int mushroomsOnPlate = mushrooms[0];
 		
 		for(int i = 0; i < numbMushrooms; i++){
 			if(mushroomsOnPlate > mushrooms[i])
 				mushroomsEaten += mushroomsOnPlate - mushrooms[i];
 			
 				mushroomsOnPlate = mushrooms[i];
 		}
 		return mushroomsEaten;
 	}
 	
 	int eatAtConstantRate(int numbMushrooms, int mushrooms[]){
 		int mushroomsEaten = 0;
 		int mushroomsOnPlate = mushrooms[0];
 		int constantVal = 0;
 		
 		for(int i = 0; i < numbMushrooms - 1; i++){
 			if(mushrooms[i] - mushrooms[i+1] > constantVal)
 				constantVal = mushrooms[i] - mushrooms[i+1];
 		}
 		
 		for(int i = 0; i < numbMushrooms - 1; i++){
 			if(mushrooms[i] < constantVal)
 				mushroomsEaten += mushrooms[i];
 			else
 				mushroomsEaten += constantVal;
 		}
 		return mushroomsEaten;
 	}
 	
 	void getInputs(int cases, Scanner sc){
 		int numbMushrooms[] = new int[cases];
 		int mushrooms[][] = new int[cases][];
 		
 		for(int i = 0; i < cases; i++){
 			numbMushrooms[i] = sc.nextInt();
 			mushrooms[i] = new int[numbMushrooms[i]];
 			for(int j =0; j < numbMushrooms[i]; j++){
 				mushrooms[i][j] = sc.nextInt();
 			}
 		}
 		
 		for(int i = 0; i < cases; i++){
 			System.out.println("Cases #" + (i+1) +": " + anyMushroomsAtAnyTime(numbMushrooms[i],mushrooms[i]) + " " + eatAtConstantRate(numbMushrooms[i], mushrooms[i]));
 		}
 	}
 	
 	public static void main(String[] args) {
 		MushroomMonster mm = new MushroomMonster();
 		Scanner sc = new Scanner(System.in);
 		int cases = sc.nextInt();
 		
 		mm.getInputs(cases, sc);
 	}
 
 }

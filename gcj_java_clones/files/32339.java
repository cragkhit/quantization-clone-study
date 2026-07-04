
 import java.util.Scanner;
 
 /**
  * Google Code Jam 2016
  * @author prasenjit
  */
 public class RankAndFile {
     int T;
     int N;
     //int grid[][];
     //int solution[][];
 
     public static void main(String args[]) {
         RankAndFile obj = new RankAndFile();
     }
     
     RankAndFile() {
         takeInputAndSolve();
     }
     
     void takeInputAndSolve() {
         Scanner sc = new Scanner(System.in);
         T = sc.nextInt();
         for(int i=0;i<T;i++) {
             N = sc.nextInt();
             int numbersTrack[] = new int[2501];
             for(int j=0;j<2*N-1;j++) {
                 for(int k=0;k<N;k++) {
                     numbersTrack[sc.nextInt()]++;
                 }
             }
             int finalList[] = new int[N];
             int counter = 0;
             for(int j=0;j<2501;j++) {
                 //System.out.println(numbersTrack[j]);
                 if(numbersTrack[j]>0) {
                     if(numbersTrack[j]%2 > 0) {//Odd number
                         finalList[counter++] = j;
                     }
                 }
                 if(counter==N) {
                     break;
                 }
             }
             //Sort final list
             for(int j=0;j<N-1;j++) {
                 for(int k=j+1;k<N;k++) {
                     if(finalList[j]>finalList[k]) {
                         int temp = finalList[j];
                         finalList[j] = finalList[k];
                         finalList[k] = temp;
                     }
                 }
             }
             //Output
             System.out.print("Case #"+(i+1)+": ");
             for(int j=0;j<N;j++) {
                 System.out.print(finalList[j]+" ");
             }
             System.out.println();
         }
     }
 }
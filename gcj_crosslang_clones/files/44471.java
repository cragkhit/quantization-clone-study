package lawnmower;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class Main {
 
     public static void main(String[] args) throws FileNotFoundException, IOException {
         File archivoEntrada = new File("C:\\Users\\Jonnathan\\Downloads\\B-large.in");
         File archivoSalida = new File("C:\\Users\\Jonnathan\\Downloads\\B-large.txt");
 
         PrintWriter salida;
         InputStream entrada = new FileInputStream(archivoEntrada);
         Scanner cin = new Scanner(entrada);
 
         int cases;
         List<String> results = new ArrayList<>();
         if (archivoSalida.exists()) {
             archivoSalida.delete();
         }
         salida = new PrintWriter(archivoSalida);
         int lowest = 100;
         if (cin.hasNextInt()) {
             cases = cin.nextInt();
             for (int k = 0; k < cases; k++) {
                 int rows = cin.nextInt();
                 int cols = cin.nextInt();
                 int m[][] = new int[rows][cols];
                 for(int i=0; i < rows; i++){
                     for(int j=0; j < cols; j++){
                         m[i][j] = cin.nextInt();
                         if(m[i][j] < lowest){
                             lowest = m[i][j];
                         }
                     }
                 }
                 results.add(testPattern(k+1,m,rows,cols,lowest));
             }
         }
         for (String r : results) {
             System.out.println(r);
             salida.println(r);
         }
         salida.close();
         entrada.close();
     }
     
     private static String testPattern(int caseNumber, int m[][], int rows, int cols, int lowest){
         StringBuilder sb = new StringBuilder("Case #");
         sb.append(caseNumber);
         sb.append(": ");
         
         boolean possible = true;
         
         for(int i=0; i < rows; i++){
             for(int j=0; j < cols; j++){
                 if(m[i][j] == lowest){
                     if(!isRowPossible(lowest,m[i],cols) && !isColPossible(lowest,m,j,rows)){
                         possible = false;
                         break;
                     }
                 }
             }
             if(!possible){
                 break;
             }
         }
         
         if(possible){
             sb.append("YES");
         } else {
             sb.append("NO");
         }
         
         return sb.toString();
     }
     
     private static boolean isRowPossible(int lowest, int[] row, int length){
         for(int i=0; i < length; i++){
             if(row[i] != lowest){
                 return false;
             }
         }
         return true;
     }
     
     private static boolean isColPossible(int lowest, int m[][], int col, int rows){
         for(int i=0; i < rows; i++){
             if(m[i][col] != lowest){
                 return false;
             }
         }
         return true;
     }
     
 }

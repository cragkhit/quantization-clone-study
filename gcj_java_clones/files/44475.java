package raj.gcj.y2016.qual;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created by rprasad on 4/9/16.
  */
 public class FractilesLarge {
     public static void main(String[] args) throws Exception{
 
         String fileName = args[0];
         FileWriter fileWriter = new FileWriter(fileName+".out");
         FractilesLarge fractiles = new FractilesLarge();
         List<String> inputs = fractiles.readInput(fileName);
         for (int i = 1; i < inputs.size() ; i++){
            String[] tokens = inputs.get(i).split(" ");
             fractiles.solve(i, fileWriter, Integer.parseInt(tokens[0]),
                     Integer.parseInt(tokens[1]),
                     Integer.parseInt(tokens[2]));
         }
 
         fileWriter.flush();
         fileWriter.close();
 
     }
 
 
 
     public static long findNextTile(Random random, double total, int K, int C){
 
 
         return (long) (random.nextDouble()*total);
     }
 
     public String solve(int caseNum, FileWriter fw, int K, int C, int S) throws IOException {
 
         String out = null;
         Random random = new Random();
         double total = Math.pow(K, C);
         List<String> positionsToCheck = new ArrayList<>();
         if (S < K){
             double expectation = (C*S)/K;
             if (expectation < 1){
                 out = String.format("Case #%d: %s", caseNum , "IMPOSSIBLE");
             }
             else {
                  positionsToCheck = new ArrayList<>(S);
                 for (int i = 0; i < S ; i++){
 
                     positionsToCheck.add(String.valueOf(findNextTile(random, total , K, C)));
                 }
 
             }
         }
         else {
              positionsToCheck = new ArrayList<>(K);
             for (int i = 0; i < K ; i++){
                 int val = i + 1;
                 positionsToCheck.add(String.valueOf(val));
             }
 
         }
         if (out == null){
             out = String.join(" " , positionsToCheck);
             out = String.format("Case #%d: %s", caseNum , out);
         }
 
         fw.write(out);
         fw.write("\n");
         System.out.println(out);
         return out;
     }
 
     public  List<String> readInput(String fileName) {
         BufferedReader br = null;
 
         List<String> lines = new ArrayList<>();
         try {
 
             String sCurrentLine;
 
             br = new BufferedReader(new FileReader(fileName));
 
             while ((sCurrentLine = br.readLine()) != null) {
                 lines.add(sCurrentLine);
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (br != null) br.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
 
         return lines;
     }
 }

/**
  *
  * @author amit
  */
 import java.io.*;
 
 public class GCJ {
 
     BufferedReader rin;
     BufferedWriter wout;
     int numCases;
 
     
     public String getNextInput() {
         try {
         	String [] line = rin.readLine().split(" ");
         	int smax = Integer.parseInt(line[0]);
             int a[] = new int[smax+1];
             int sum = 0;
             int n = 0;
             int nsum = 0;
             //System.out.println("in:" + line[1]);
             for (int i=0; i<smax+1; i++) {                
                 //System.out.print(line[1].charAt(i));
                 a[i] = line[1].charAt(i)-48;
                 n = i - sum;
                 sum += a[i];
                 if (n > 0) {
                     sum += n;
                     nsum += n;
                 }                    
             }
         	return Integer.toString(nsum);
         	
         } catch (IOException e) {
             System.out.println("File Error! Could not read line from file");
         }
         return null;
     }
 
     public void writeNextOutput(String s) {
         try {
             wout.write(s);
             wout.write("\n");
         } catch (IOException e) {
             System.out.println("Error! Could not write to the file!");
         }
     }
 
     public GCJ(String s1, String s2) {
         try {
             rin = new BufferedReader(
                     new FileReader(s1));
             wout = new BufferedWriter(new FileWriter(s2));
             numCases = Integer.parseInt(rin.readLine());
 
             for(int i=1; i <= numCases; i++){
                 System.out.println("case#" + i);
                 String sout = getNextInput();
                 sout = "Case #" + Integer.toString(i) + ":" + " " + sout;
                 writeNextOutput(sout);
                 //System.out.println("case#" + i);
             }
             rin.close();
             wout.close();
         } catch (IOException e) {
             System.out.println("File error");
             System.exit(0);
         }
     }
 
     public static void main(String[] args) {/*
         if (args.length!=2) {
         System.out.println("Enter input file!");
         System.exit(0);
         }*/
         GCJ a = new GCJ("infile.txt", "out.txt");
     }
 }

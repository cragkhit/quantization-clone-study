/*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package googlecodejam.qual;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 /**
  *
  * @author ozdemir
  */
 public class partB {
 
     public static void main(String argv[]) throws FileNotFoundException {
         Scanner in = new Scanner(new File("input.txt"));
         PrintStream out = new PrintStream(new File("output.txt"));
 
         int T = in.nextInt();
         for (int i = 0; i < T; i++) {
             out.println("Case #" + (i + 1) + ": "
                     + process(in.nextFloat(), in.nextFloat(), in.nextFloat()));
         }
     }
 
     private static double process(double C, double F, double X) {
         double rate = 2.0;
         double time = 0;
 
         while (X / rate > C / rate + X / (rate + F)) {
             time += C / rate;
             rate += F;
         }
         return time + X / rate;
     }
 }

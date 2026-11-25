/*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package r1;
 
 
 import java.util.Scanner;
  
 public class R1 {
 
 
     public static void main(String[] args) {
         Scanner scan = new Scanner(System.in);
         int T = 0, N = 0;
         int y = 0;
         int z = 0;
         int rate = 0;
         int temp = 0;
         T = scan.nextInt();
 
         for (int i = 0; i < T; i++) {
             N = scan.nextInt();
             int[] m = new int[N];
             for (int j = 0; j < N; j++) {
                 m[j] = scan.nextInt();
             }
             y = 0;
             z = 0;
             rate = 0;
             temp = 0;
             for (int j = 0; j < N - 1; j++) {
                 if (m[j] > m[j + 1]) {
                     y += m[j] - m[j + 1];
                     temp = (m[j] - m[j + 1]);
                     if (temp > rate) {
                         rate = temp;
                     }
                 }
             }
             for (int j = 0; j < N - 1; j++) {
                 if (m[j]-rate  <0) {
                     z += m[j];
                 } else {
                     z += rate;
                 }
             }
             System.out.println("Case #" + (i + 1) + ": " + y + " " + z);
 
         }
       
     }
 }   
 

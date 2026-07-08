import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class Main {
 
     public static boolean a[];
     public static int n, sum, count;
     public static FileWriter fw;
 
     public static void main(String[] args) {
         Scanner scanner = null;
         try {
             fw = new FileWriter("A-large.out");
         } catch (IOException e) {
             e.printStackTrace();
         }
         try {
             scanner = new Scanner(new File("A-large.in"));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         int q = scanner.nextInt();
         for (int t=1;t<=q;t++) {
             n = scanner.nextInt();
             a = new boolean[10];
             count = 10;
             sum = 0;
             if (n==0) {
 //                System.out.println("Case #" + t + ": INSOMNIA");
                 try {
                     fw.write("Case #" + t + ": INSOMNIA\n");
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } else {
                 while (true) {
                     sum += n;
                     int tmp = sum;
                     int d;
                     while (tmp > 0) {
                         d = tmp % 10;
                         if (!a[d]) {
                             a[d] = true;
                             count--;
                             if (count == 0) {
 //                                System.out.println("Case #" + t + ": " + sum);
                                 try {
                                     fw.write("Case #" + t + ": " + sum + "\n");
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                                 break;
                             }
                         }
                         tmp = tmp / 10;
                     }
                     if (count == 0) {
                         break;
                     }
                 }
             }
         }
         try {
             fw.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }

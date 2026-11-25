import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 class Ovation {
     public static void main(String[] args) {
         try (Scanner sc = new Scanner(new File("input.in"));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                         "A-output.out")))) {
             
             int n = sc.nextInt();
             for (int i = 0; i < n; i++) {
                 String answer = "Case #" + (i + 1) + ": "
                         + solve(sc.nextInt(), sc.next());
                 bw.write(answer);
                 bw.newLine();
                 System.out.println(answer);
             }
             bw.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     private static int solve(int n, String ov) {
         int result = 0;
         int sum = 0;
         for (int i = 0; i <= n; i++) {
             if (i - sum > 0) {
                 result += i - sum;
                 sum = i;
             }
             sum += ov.charAt(i) - '0';
         }
         return result;
     }
 }

import java.util.Scanner;
    import java.io.*;
 
 public class StandingOvation {
     public static void main(String[] args) throws IOException {
         Scanner scanner = new Scanner(System.in);
         int numCases = Integer.parseInt(scanner.nextLine());
         
         PrintWriter out = new PrintWriter("StandingOvation.out");
         
         for (int i = 1; i <= numCases; i++) {
             String[] testCase = scanner.nextLine().split(" ");
             int maxShyness = Integer.parseInt(testCase[0]);
             String digits = testCase[1];
             int minFriends = 0;
             int runningSum = 0;
             
             for (int j = 0; j <= maxShyness; j++) {
                 if (runningSum < j) {
                     minFriends += j - runningSum;
                     runningSum = j;
                 }
                 int digit = Integer.parseInt(digits.substring(j,j+1));
                 runningSum += digit;
             }
 
             out.println("Case #" + i + ": " + minFriends);
         }
         out.close();
     }
 }

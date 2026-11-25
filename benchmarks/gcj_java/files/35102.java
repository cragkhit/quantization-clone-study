package google.code.jam;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.Scanner;
 
 public class StandingOviation
 {
 
    public static int friendsRequired(int maxShyness, String audienceShyness) {
       int friendsCount = 0;
       int standingCount = 0;
       
       //for each shyness level
       for (int shyness = 0; shyness <= maxShyness; shyness++) {
          
          //get the number of people at current shyness level
          int audience = Integer.parseInt(Character.toString(audienceShyness.charAt(shyness)));
          if (audience > 0) {
             if (standingCount >= shyness) {
                //we have required number of people already standing, add them to the current standing count
                standingCount = standingCount + audience;
             }
             else {
                //we need friends help
                friendsCount = friendsCount + (shyness - standingCount);
                standingCount = standingCount + (shyness - standingCount) + audience;
             }
          }
          
       }
      
       return friendsCount;
    }
    public static void main(String[] args) throws IOException
    {
       Path testFile = Paths.get("/home/mneeraj/Desktop/test.txt");
       Path resultFile = Paths.get("/home/mneeraj/Desktop/result.txt");
       Scanner s = new Scanner(testFile);
       BufferedWriter result = Files.newBufferedWriter(resultFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
 
       int numTests = s.nextInt();
       int count = 1;
       while(count <= numTests) {
          
          result.write("Case #" + count + ": " + friendsRequired(s.nextInt(), s.next()));
          result.newLine();
          count++;
          
       }
       
       s.close();
       result.close();      
 
    }
 
 }

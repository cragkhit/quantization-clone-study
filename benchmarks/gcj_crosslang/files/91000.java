import java.io.*;
 import java.awt.*;
 import java.util.*;
 public class ProblemB
 {
   public static void main (String [] args) throws Exception
   {
     Scanner in = new Scanner (new FileReader ("B-large.in"));
     PrintWriter out = new PrintWriter (new FileWriter ("out.txt"));
     int cases = in.nextInt ();
     in.nextLine();
     for (int x = 0; x < cases; x++)
     {
       int diners = in.nextInt();
       int [] pancakes = new int [diners];
       for (int y = 0; y < diners; y++)
       {
         pancakes[y] = in.nextInt();
       }
       Arrays.sort (pancakes);
       int [] temp = new int [pancakes.length];
       for (int y = 1; y <= pancakes.length; y++)
       {
         temp [y-1] = pancakes [pancakes.length - y];
       }
       pancakes = temp;
       int first = pancakes [0];
       int special = 0;
       int current = 0;
       int [] minsEach = new int [first];
       for (int y = 1; y <= first ;y++)
       {
         special = 0;
         for (int m = 0; m < pancakes.length; m++)
         {
           current = pancakes [m];
           if (current%y == 0)
             current = current - y;
           else
             current = current - (current%y);
           special = special + (current/y);
         }
         minsEach [y-1] = special + y;
       }
       Arrays.sort (minsEach);
       out.println ("Case #" + (x+1)+": " + minsEach [0]);
     }
     out.close();
   }
 }
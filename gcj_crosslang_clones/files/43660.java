
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class standing{
 
     public static void main(String[] args) throws IOException {
     int t=input();
     for(int j=1;j<=t;j++){
         s = br.readLine().split(" ");
         
     int ans=0,count=0;
         
     for (int i = 0; i < s[1].length(); i++) {
             if(s[1].charAt(i)!='0'){
                 
             if(count<i){
             ans+=(i-count);
             count=i;
             }
                 count+=s[1].charAt(i)-'0';
                 
             }
             
             
         }
         
         System.out.println("Case #"+j+": "+ans);
     
     }
     
     
     
     }
     
         static BufferedReader br = new BufferedReader(new InputStreamReader(
             System.in));
     private static String s[], w, q;
 
     public static void input(int a[], int p) throws IOException {
         s = br.readLine().trim().split(" ");
         int i;
         for (i = 0; i < p; i++) {
             a[i] = Integer.parseInt(s[i]);
         }
 
     }
 
     public static void input(long a[], int p) throws IOException {
         s = br.readLine().trim().split(" ");
         int i;
         for (i = 0; i < p; i++) {
             a[i] = Long.parseLong(s[i]);
         }
     }
 
     public static int input() throws IOException {
         int ab;
         ab = Integer.parseInt(br.readLine().trim());
         return ab;
     }
 
 }


 
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 public class test {
     public static void main (String[] args) {
         Scanner in = new Scanner(System.in);
         int T = in. nextInt();
         for (int i = 1; i <= T; i++) {
             System.out.printf("Case #%d: ",i);
             int N = in.nextInt();
             int[] P= new int[N];
             int sum =0;
             PriorityQueue<Pair> q= new PriorityQueue<>();
             for (int j = 0; j <N ; j++) {
                 P[j]= in.nextInt();
                 sum+=P[j];
                 q.add(new Pair(j,P[j]));
             }
             while(!q.isEmpty()){
                 Pair k = q.remove();
                 Pair l = q.remove();
                 if (k.w==1 && q.isEmpty()){
 
                     System.out.print((char)(k.e+'A'));
                     System.out.print((char)(l.e+'A'));
                     System.out.print(" ");
 
 
                     break;
                 }
                 if (k.w==1){
 
                     System.out.print((char)(k.e+'A'));
                     System.out.print(" ");
 
                     k.w--;
                     sum-=1;
 
                 }
                 else if(l.w> (sum-1)/2){
                     System.out.print((char)(k.e+'A'));
                     System.out.print((char)(l.e+'A'));
                     System.out.print(" ");
 
                     k.w--;
                     l.w--;
                     sum-=2;
 
                 }else if(l.w<=(sum-2)/2 && (k.w>2 ||(k.w==2&&q.size()>=1))){
                     System.out.print((char)((k.e+'A')));
                     System.out.print((char)((k.e+'A')));
                     System.out.print(" ");
 
                     k.w--;
                     k.w--;
                     sum-=2;
 
                 }else{
                     System.out.print((char)(k.e+'A'));
                     System.out.print(" ");
 
                     k.w--;
                     sum-=1;
 
 
                 }
                 if(k.w>0){
                     q.add(k);
                 }
                 if(l.w>0){
                     q.add(l);
                 }
             }
             System.out.println();
 
 
         }
     }
     private static class Pair implements Comparable<Pair>{
         int e;
         int w;
         public Pair(int e, int w) {
             this.e = e;
             this.w = w;
         }
 
         @Override
         public int compareTo(Pair o) {
             return o.w-this.w;
         }
     }
 }

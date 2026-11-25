import java.util.*;
 import java.math.BigInteger;
 
 public class Main {
 
   public static void main(String args[]) {
     (new Main()).solve();
   }
 
   long solve(BigInteger MAX) {
 
     long ret = 0;
     if( MAX.compareTo(new BigInteger("1")) >= 0 ) { ++ret; }
     if( MAX.compareTo(new BigInteger("4")) >= 0 ) { ++ret; }
     if( MAX.compareTo(new BigInteger("9")) >= 0 ) { ++ret; }
 
     BigInteger two = new BigInteger("2");
     BigInteger ten = new BigInteger("10");
     BigInteger cur = new BigInteger("2");
     while( true ) {
       cur = cur.multiply(ten);
       if( MAX.compareTo( (cur.add(two)).multiply(cur.add(two)) ) >= 0 ) { ++ret; }
       else { break; }
     }
 
     for(int LEN=2; LEN<=100; ++LEN) {
       // LEN - 1 & 0;
       BigInteger base = pow(ten, LEN - 1).add(pow(ten, 0));
       if( MAX.compareTo(base.multiply(base)) < 0 ) { break; }
       // base OK;
       ++ret;
       int ENDS = LEN / 2;
     XLOOP:
       for(int X=LEN-2; X>=ENDS; --X) {
         BigInteger XX = base.add(pow(ten, X));
         if( X != (LEN - 1 - X) ) {
           XX.add(pow(ten, LEN - 1 - X));
         }
         if( MAX.compareTo(XX.multiply(XX)) < 0 ) { continue XLOOP; }
         // XX OK;
         ++ret;
       YLOOP:
         for(int Y=X-1; Y>=ENDS; --Y) {
           BigInteger YY = XX.add(pow(ten, Y));
           if( Y != (LEN - 1 - Y) ) {
             YY.add(pow(ten, LEN - 1 - Y));
           }
           if( MAX.compareTo(YY.multiply(YY)) < 0 ) { continue YLOOP; }
           // YY OK;
           ++ret;
           for(int Z=Y-1; Z>=ENDS; --Z) {
             BigInteger ZZ = YY.add(pow(ten, Z));
             if( Z != (LEN - 1 - Z) ) {
               ZZ.add(pow(ten, LEN - 1 - Z));
             }
             if( MAX.compareTo(ZZ.multiply(ZZ)) < 0 ) { continue; }
             // ZZ OK;
             ++ret;
             if( (LEN - 1) % 2 == 0 && Z > (LEN - 1) / 2 ) {
               ZZ.add(pow(ten, (LEN - 1) / 2));
               if( MAX.compareTo(ZZ.multiply(ZZ)) >= 1 ) { ++ret; }
             }
           }
         }
       }
     }
 
     return ret;
 
   }
 
   BigInteger pow(BigInteger A, int pos) {
     if( pos == 0 ) { return BigInteger.ONE; }
     if( pos % 2 == 0 ) { return pow(A.multiply(A), pos / 2); }
     return pow(A.multiply(A), pos / 2).multiply(A);
   }
 
   void solve() {
 
     Scanner cin = new Scanner(System.in);
 
     int T = cin.nextInt();
     for(int C=1; C<=T; ++C) {
 
       BigInteger A = new BigInteger(cin.next());
       BigInteger B = new BigInteger(cin.next());
 
       System.out.println("Case #" + C + ": " + (solve(B) - solve(A.subtract(BigInteger.ONE))));
 
     }
 
   }
 
 }

import java.util.*;
 import java.math.BigInteger;
 
 public class pals
 {
 
     //returns the largest integer that is smaller than or equal to the 
     //square root of n.  Newton's Method.
     public static BigInteger sqrt(BigInteger n){
 	BigInteger r2 = BigInteger.ONE.shiftLeft(n.bitLength()/2);
 	BigInteger r;
 	//Scanner in = new Scanner(System.in);
 	do{
 	    r = r2;
 	    r2 = r.multiply(r).add(n).divide(r.multiply(BigInteger.ONE.shiftLeft(1)));
 	    //  System.out.println("r = " + r);
 	    //in.next();
 	}while(!r.equals(r2) && !r.add(BigInteger.ONE).equals(r2));
 	while(r.add(BigInteger.ONE).multiply(r.add(BigInteger.ONE)).compareTo(n) <= 0)
 	    r = r.add(BigInteger.ONE);
 	
 	return r;
     }
 
     public static long Ctbl[][];
     
 
     public static long C(int n, int dst)
     {   
 	if(n < 0 || dst < 0)
 	    return 0;
 	
 	if(Ctbl[n][dst] < 0) {
 	    if( n >= 2){
 		Ctbl[n][dst] = C(n-2,dst) + C(n-2,dst-2) + C(n-2,dst-8);
 	    }
 	    else if( n == 1){
 		Ctbl[n][dst] = 1;
 		if(dst >= 1) Ctbl[n][dst]++;
 		if(dst >= 4) Ctbl[n][dst]++;
 		//if(dst >= 9) Ctbl[n][dst]++;
 	    }
 	    else{ // n == 0
 		if(dst == 9)
 		    Ctbl[n][dst] = 0;
 		else
 		    Ctbl[n][dst] = 1;
 	    }
 	}
     	return Ctbl[n][dst];
     }
     
     public static long countUpTo(String num)
     {
 	//System.out.println("num = " + num);
 	long ans = 0;
 		
 	if(num.length() == 1){
 	    int n = num.charAt(0) - '0';
 	    if(n > 3) ans = 3;
 	    else ans = n;
 	}else{
 	    //condition on first place we are less...
 	    int dst = 9;
 	    int n = num.length();
 	    for(int d = 1; d < (num.charAt(0) - '0'); d++)
 		ans += C(n-2,dst - 2*d*d);
 	    dst -= 2*(num.charAt(0)-'0')*(num.charAt(0)-'0');
 	    for(int i = 1; i < n/2; i++){
 		for(int d = 0; d < (num.charAt(i) - '0'); d++)
 		    ans += C(n-2*(i+1),dst - 2*d*d);
 		dst -= 2*(num.charAt(i)-'0')*(num.charAt(i)-'0');
 	    }
 	    if(dst >= 0){
 		boolean palBelow = true;
 		boolean strict = false;
 		int r = n/2;
 		int l = r - 1;
 		if(n%2 == 1)
 		    r++;
 		while(l >= 0 && palBelow && !strict){
 		    if(num.charAt(l) == num.charAt(r)){
 			r++; l--;
 		    }
 		    else if(num.charAt(l) < num.charAt(r))
 			strict = true;
 		    else
 			palBelow = false;
 		}
 		if(palBelow) ans++;
 	    }
 	    ans -= C(n-2,9);
 	    for(int i = 1; i < n; i++)
 		ans += C(i,9);
 	}
 	//System.out.println("ans = " + ans);
 	return ans;
     }
 
     public static void main(String args[])
     {
 	Scanner in = new Scanner(System.in);
 	Ctbl = new long[60][10];
 	for(int i = 0; i < 60; i++)
 	    for(int j = 0; j < 10; j++)
 		Ctbl[i][j] = -1;
 
 	int T = in.nextInt();
 	for(int caseno = 1; caseno <= T; caseno++){
 	    System.out.print("Case #" + caseno +": ");
 	    BigInteger A, B;
 	    A = in.nextBigInteger();
 	    B = in.nextBigInteger();
 	    
 	    BigInteger rA, rB;
 	    rA = sqrt(A); rB = sqrt(B);
 	    if(rA.multiply(rA).compareTo(A) >= 0)
 		rA = rA.subtract(BigInteger.ONE);
 	    
 	    System.out.println(countUpTo(rB.toString()) - countUpTo(rA.toString()));
 	}
     }
 
 }
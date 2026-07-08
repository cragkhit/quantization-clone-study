import java.util.Scanner;
 
 
 public class Dijkstra {
 	static boolean carry=false;static String s="", originalS;static int exhausted=0,l,x;
 	public static void main(String[] args) {
 
 		int T; Scanner scan = new Scanner(System.in);
 		T=scan.nextInt();
 		for(int i11=1;i11<=T;i11++){
 			l=scan.nextInt(); long x1=scan.nextLong(); carry= false;
 			x=(int)x1%100;
 			s=scan.next();
 			s=s.replaceAll("i", "2").replaceAll("j", "3").replaceAll("k", "4");
 			exhausted=1;originalS=s+"";
 			try{
 				s=getI(s,2);
 				s=getI(s,3);
 				s=getI(s,4);}
 			catch(Exception e){
 				System.out.printf("Case #%d: %s\n",i11,"NO");
 				continue;
 			}
 			System.out.printf("Case #%d: %s\n",i11,is1(s)&&!carry?"YES":"NO");
 		}
 		scan.close();
 	}
 
 	private static boolean is1(String s2) {
 		//System.out.println("is1.........."+s2);
 		Object[] temp1=getVal(originalS);
 		int temp = (int)temp1[0]; boolean bkpF=(boolean)temp1[1];int mul=0;
 		if(x-exhausted<10)
 			s2=getConcatStr(s2, x-exhausted);
 		else s2=getConcatStr(s2, (4+(x-exhausted)%4));
 		int last=1;
 		for(int i=0;i<s2.length();i++){
 			last=mul(""+last,s2.charAt(i)+"");
 		}
 		return last==1;
 	}
 	private static Object[] getVal(String s2) {
 		int last=1;boolean bkp=carry&&true,temp;
 		for(int i=0;i<s2.length();i++){
 			last=mul(""+last,s2.charAt(i)+"");
 		}
 		temp=carry!=bkp;carry=bkp;
 		return new Object[]{last,temp};
 	}
 
 	private static String getConcatStr(String s2, int x) {
 		StringBuilder  sb = new StringBuilder(s2);
 		for(int i=0;i<x;i++)
 			sb.append(originalS);
 		return sb.toString();
 	}
 	private static String getI(String s1, int j) {
 		int last=1;
 		boolean bkpCarry=carry&&true;
 		for(int i=0;i<s1.length();i++){
 			if(last==j){
 				s1= s1.substring(i);
 				return s1;	
 			}
 			if(((s1.charAt(i)==48+j)&&last==1)){
 				s1= s1.substring(i+1);
 				return s1;
 			}
 			else {
 				last=mul(""+last,s1.charAt(i)+"");
 			}
 			//System.out.println(last);
 		}
 		if(last==j){
 			s1= "";
 			return s1;	
 		}
 		if(exhausted<x)
 		{
 			exhausted++;
 			//System.out.println("exhausted..."+exhausted + " "+x+ " "+(exhausted<x));
 			s1=getConcatStr(s1, 1);
 			carry=bkpCarry;
 			return getI(s1,j);
 		}
 		else return null;
 	}
 
 	private static int mul(String x1, String y1) {
 		//	System.out.println("Entered....."+x1+"  "+y1);
 		int x=Integer.valueOf(x1),y=Integer.valueOf(y1);
 
 		int diff=y-x;int ans=0;
 		if(x==1)
 			ans=y;
 		else if(y==1)
 			ans=x;
 		else if(diff==1)
 			ans=1+y;
 		else if(diff==-2)
 			ans=3;
 		else if(diff==-1)
 		{ans=1+x;carry=!carry;}
 		else if(diff==2)
 		{ans=3;carry= !carry;}
 		else if(diff==0)
 		{ans=1;carry=!carry;}
 		if(ans==5)
 			return 2;
 		return ans;
 	}
 
 }

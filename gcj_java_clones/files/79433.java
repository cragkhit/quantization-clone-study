package codejam2014;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class countingsheep {
 
 	/**
 	 * @param args
 	 */
 	static String INPUT_LOCATION = "C:\\Users\\srihari\\workspace\\codejam2016\\";
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 
 		Scanner sc = new Scanner(new FileReader(INPUT_LOCATION
 				+ "A-large.in"));
 		PrintWriter pw = new PrintWriter(new FileWriter(INPUT_LOCATION
 				+ "A-large.in.out"));
 		 
 		//Scanner sc=new Scanner(System.in);
 		int cnttests = sc.nextInt();
 		for (int i = 0; i < cnttests; i++) 
 			{
 				int n = sc.nextInt();
 				if(n==0)
 				{
 					pw.println("Case #"+(i+1)+": INSOMNIA");
 					//System.out.println("Case #"+(i+1)+": INSOMNIA");
 				}
 				else 
 				   pw.println("Case #"+(i+1)+": "+ finalnumber(n));
 				   //System.out.println("Case #"+(i+1)+": "+ finalnumber(n));
 			}
 			
 		pw.flush();
 		pw.close();
 		sc.close();
 
 	   }
 		private static int finalnumber(int n)
 		{
 			StringBuilder finalvalue = new StringBuilder("1111111111");
 			StringBuilder current = new StringBuilder("0000000000");
 			int i = 0;
 			while(finalvalue.toString().compareTo(current.toString())!=0)
 			{
 				i=i+1;
 				int temp = n*i;
 				while(true)
 				{
 					if(temp/10==0)
 					{
 						current.setCharAt(temp,'1');
 						break;
 					}
 					else
 					{
 						current.setCharAt(temp%10,'1');
 						temp= temp/10;
 					}
 					
 				}
 				
 			}
 			return n*i;
 		}
 }

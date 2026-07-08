import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 
 public class getdigit {
 
 	public static void main(String[] args) throws IOException {
 		// TODO Auto-generated method stub
 		int T,i,j,EC=0,FC=0,GC=0,HC=0,IC=0,NC=0,OC=0,RC=0,SC=0,TC=0,UC=0,VC=0,WC=0,XC=0,ZC=0,k;
 		int zero,one,two,three,four,five,six,seven,eight,nine,avlstr=0;
 		String [] instr = new String[100];
 		Scanner filein = new Scanner(new File("C:/codejamdata/A-large.in"));
 		PrintStream fileout = new PrintStream(new FileOutputStream("C:/codejamdata/gcj1abloutput.txt"));
 		T = filein.nextInt();
 		for(i=0;i<T;i++)
 		{
 			instr[i] = filein.next();
 			EC=0;FC=0;GC=0;HC=0;IC=0;NC=0;OC=0;RC=0;SC=0;TC=0;UC=0;VC=0;WC=0;XC=0;ZC=0;
 			for(j=0;j<instr[i].length();j++)
 			{
 				if(instr[i].charAt(j)=='E') EC++;
 				if(instr[i].charAt(j)=='F') FC++;
 				if(instr[i].charAt(j)=='G')	GC++;
 				if(instr[i].charAt(j)=='H')	HC++;
 				if(instr[i].charAt(j)=='I')	IC++;
 				if(instr[i].charAt(j)=='N')	NC++;
 				if(instr[i].charAt(j)=='O')	OC++;
 				if(instr[i].charAt(j)=='R')	RC++;
 				if(instr[i].charAt(j)=='S')	SC++;
 				if(instr[i].charAt(j)=='T')	TC++;
 				if(instr[i].charAt(j)=='U')	UC++;
 				if(instr[i].charAt(j)=='V')	VC++;
 				if(instr[i].charAt(j)=='W')	WC++;
 				if(instr[i].charAt(j)=='X')	XC++;
 				if(instr[i].charAt(j)=='Z')	ZC++;
 			}
 			avlstr=instr[i].length();
 			zero=0;one=0;two=0;three=0;four=0;five=0;six=0;seven=0;eight=0;nine=0;
 			while(avlstr>0)
 			{
 				//Check for zero
 				if(ZC>0)
 				{
 					zero=ZC;
 					RC= RC-ZC; OC= OC-ZC; EC= EC-ZC; avlstr = avlstr-4*ZC;
 					ZC=0;
 				}
 				//Check for two
 				if(WC>0)
 				{
 					two=WC;
 					TC=TC-WC; OC=OC-WC; avlstr=avlstr-3*WC;
 					WC=0;
 				}
 				//Check for 4
 				if(UC>0)
 				{
 					four=UC;
 					FC = FC-UC; OC= OC-UC; RC= RC-UC; avlstr=avlstr-4*UC;
 					UC=0;				
 				}
 				//Check for 6
 				if(XC>0)
 				{
 					six=XC;
 					IC = IC-XC; SC= SC-XC; avlstr=avlstr-3*XC;
 					XC=0;
 				}
 				//Check for 8
 				if(GC>0)
 				{
 					eight=GC;
 					EC = EC-GC; IC=IC-GC; HC=HC-GC;;TC=TC-GC; avlstr=avlstr-5*GC;
 					GC=0;
 				}
 				//Check for 1
 				if(OC>0 && NC>0)
 				{
 					one=OC;
 					NC=NC-OC; EC=EC-OC; avlstr=avlstr-3*OC;
 					OC=0;
 				}
 				//Check for 3
 				if(TC>0 && RC>0)
 				{
 					three=TC;
 					HC=HC-TC; RC=RC-TC; EC=EC-2*TC; avlstr=avlstr-5*TC;
 					TC=0;
 				}
 				//Check for 5
 				if(FC>0)
 				{
 					five=FC;
 					IC=IC-FC; VC=VC-FC;EC=EC-FC; avlstr=avlstr-4*FC;
 				}
 				//Check for 7
 				if(SC>0)
 				{
 					seven=SC;
 					EC=EC-2*SC; VC=VC-SC; NC=NC-SC; avlstr=avlstr-5*SC;
 					SC=0;
 				}
 				//Check for 9
 				if(IC>0)
 				{
 					nine=IC;
 					NC=NC-2*IC; EC=EC-IC; avlstr= avlstr-4*IC;
 					IC=0;
 				}
 				
 			}
 			fileout.print("Case #" + (i+1) + ": " );
 			for(k=0;k<zero;k++) fileout.print('0');
 			for(k=0;k<one;k++) fileout.print('1');
 			for(k=0;k<two;k++) fileout.print('2');
 			for(k=0;k<three;k++) fileout.print('3');
 			for(k=0;k<four;k++) fileout.print('4');
 			for(k=0;k<five;k++) fileout.print('5');
 			for(k=0;k<six;k++) fileout.print('6');
 			for(k=0;k<seven;k++) fileout.print('7');
 			for(k=0;k<eight;k++) fileout.print('8');
 			for(k=0;k<nine;k++) fileout.print('9');
 			fileout.println();
 		}
 		filein.close();
 		fileout.close();
 	}
 
 }

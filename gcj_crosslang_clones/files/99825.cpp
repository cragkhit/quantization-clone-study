#include <iostream>
 #include <sstream>
 #include <stdio.h>
 #include <string.h>
 #include <fstream>
 using namespace std;
 
 char  p[6][6];
 int q[6][6],r[6][6],d;
 //int dig[2],ro[4],cl[4]; 
 void tick()
 {   int i,j;
 	for(i=1;i<5;i++)
 		{
 		for(j=1;j<5;j++)
 			{
 			if(p[i-1][j-1]=='X')
 			    {
 			      q[i][j]+=q[i][j-1]+1; 
 			      r[i][j]+=r[i-1][j]+1;
 			       		
 			    }
 			else if(p[i-1][j-1]=='O')
 			    {
 			    	q[i][j]+=q[i][j-1]-1;
 			    	r[i][j]+=r[i-1][j]-1;
 			    }
 			else if(p[i-1][j-1]=='T')
 				{
 					if(q[i][j-1] > 0) q[i][j]+=q[i][j-1]+1;
 					else if (q[i][j-1] < 0) q[i][j] += q[i][j-1] - 1;
 					if(r[i-1][j] >0 ) r[i][j]+=r[i-1][j]+1;
 					else r[i-1][j]+=r[i-1][j]-1;
 				}
 			else 
 			    {
 			    d=1;
 			    q[i][j]+=q[i][j-1];
 			    r[i][j]+=r[i-1][j];
 			    }
 			}
 		}
 }
 int main()
 {
 int n=0,i=0,j,tt=1;
 string st;
 ifstream fin("input2.txt");
 ofstream fout("output2.txt");
 fin>>st;
 while(st[i]!='\0')
 	{n=n*10 + st[i]-'0';i++;}
 //cout<<t;
 while(tt<=n)
 {
 d=0;
 memset(q,0,36*sizeof(int));
 memset(r,0,36*sizeof(int));
 for(i=0;i<4;i++)
 	for(j=0;j<4;j++)
 fin>>p[i][j];
 tick();
 fout<<"Case #"<<tt<<": ";
 int e=1;
 for(i=1;i<=4;i++)
 {if(q[i][4]==4 ||r[4][i]==4)
      {fout<<"X won\n";e=0;break;}
  else if(q[i][4]==-4||r[4][i]==-4)
  	 {fout<<"O won\n";e=0;break;}
  else if( q[i][4]==3 && p[i-1][0]=='T' )
  	 {fout<<"X won\n";e=0;break;}
  else if( q[i][4]==-3 && p[i-1][0]=='T' )
  	 {fout<<"O won\n";e=0;break;}
  else if( r[4][i]==3 && p[0][i-1]=='T' )
  	 {fout<<"X won\n";e=0;break;}
  else if( r[4][i]==-3 && p[0][i-1]=='T' )
  	 {fout<<"O won\n";e=0;break;}
 }
 if(e){
 i=0;
 while(i<4)
 {	if(p[i][i]=='X'|| p[i][i]=='.')
     break;
     i++;
 }
 if(i==4)  {fout<<"O won\n";e=0;}
 else if(e)
 {
 	i=0;
 while(i<4)
 {	if(p[i][i]=='O'|| p[i][i]=='.')
     break;
     i++;
 }
 if(i==4)  {fout<<"X won\n";e=0;}
 } 
 }
 //for diagonal...
 if(e){
 i=0;
 while(i<4)
 {	if(p[i][3-i]=='X'|| p[i][3-i]=='.')
     break;
     i++;
 }
 if(i==4)  {fout<<"O won\n";e=0;}
 else if(e)
 {
 	i=0;
 while(i<4)
 {	if(p[i][3-i]=='O'|| p[i][3-i]=='.')
     break;
     i++;
 }
 if(i==4)  {fout<<"X won\n";e=0;}
 } 
 } 
 if(e && d) fout<<"Game has not completed\n";
 else if(e) fout<<"Draw\n";
 //cout<<" "<<f;
 getline(fin,st);
 //cout<<"is over "<<test;
 tt++;
 }
 }

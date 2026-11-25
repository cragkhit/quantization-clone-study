#include<iostream>
 #include<stdio.h>
 #include<string>
 using namespace std;
 int X=0,S=1,V=2,I=3,U=4,F=5,Z=6,E=7,R=8,O=9,N=10,T=11,W=12,H=13,G=14;
 int check(int a[])
 {
 	for(int i=0;i<26;i++)
 	{
 
 		if(a[i]>0)
 			return true;
 	}
 	return false;
 }
 int main()
 {
 
 
 	int t;
 	cin>>t;
 	for(int i=0;i<t;i++)
 	{
 		int x[10]={0};
 		int n;
 		string s;
 		int a[26]={0};
 		cin>>s;
 		for(int k=0;k<s.size();k++)
 			a[s[k]-65] += 1;
 		
 
 				while(a[23]>0)//six
 				{
 					--a[23];
 					--a[8];
 					--a[18];
 					++x[6];
 					continue;
 				}
 				while(a[20]>0)//four
 				{
 					--a[20];
 					--a[5];
 					--a[14];
 					--a[17];
 					++x[4];
 					continue;
 
 				}
 
 				while(a[25]>0)//zero
 				{
 
 					--a[4];
 					++x[0];
 					--a[25];
 					--a[17];
 					--a[14];
 					continue;
 
 
 				}
 
 				while(a[22]>0)//two
 				{
 
 					--a[19];
 					++x[2];
 					--a[22];
 					--a[14];
 					continue;
 
 				}
 				while(a[6]>0)//eight
 				{
 
 					--a[4];
 					--a[19];
 					--a[8];
 					++x[8];
 					--a[6];
 					--a[7];
 					continue;
 
 				}
 			
 
 				while(a[18]>0)//seven
 				{
 
 					a[4]-=2 ;
 					--a[21];
 					--a[13];
 					++x[7];
 					--a[18];
 					continue;
 				}
 				while(a[21]>0)//five
 				{
 
 					--a[5];
 					++x[5];
 					--a[4];
 					--a[8];
 					--a[21];
 					continue;
 
 				}
 				while(a[17]>0)//three
 				{
 
 					a[4]-=2;
 					--a[19];
 					--a[7];
 					++x[3];
 					--a[17];
 					continue;
 
 				}
 				while(a[14]>0)//one
 				{
 
 					--a[4];
 					--a[13];
 					--a[14];
 					++x[1];
 					continue;
 				}
 				while(a[4]>0)//nine
 				{
 					--a[13];
 					--a[13];
 					--a[8];
 					++x[9];
 					--a[4];
 					continue;
 
 				}
 				
 			cout<<"Case #"<<i+1<<": ";
 			for(int j=0;j<10;j++)
 			{
 				int temp=x[j];
 				while(temp--)
 					printf("%d",j);
 			}
 			cout<<endl;
 	}
 
 
 
 }
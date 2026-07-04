#include<bits/stdc++.h>
 using namespace std;
 
 int main(int argc, char const *argv[])
 {
 	int t;
 	cin>>t;
 	for(int k = 0; k < t; k++) {
 	string x;
 	int arr[100]={0};
 	int dig[10]={0};
 	for (int i = 0; i < 100; ++i)
 	{
 		arr[i]=0;
 	}
 	for (int i = 0; i < 10; ++i)
 	{
 		dig[i]=0;
 	}
 	cin>>x;
 	for(int	i = 0; i < x.length();i ++) {
 		arr[x[i]]++;
 	}
 		dig[0]=arr['Z'];
 		arr['E']-=arr['Z'];
 		arr['R']-=arr['Z'];
 		arr['O']-=arr['Z'];
 		arr['Z']=0;
 		dig[6]=arr['X'];
 		arr['I']-=arr['X'];
 		arr['S']-=arr['X'];
 		arr['X']=0;
 		dig[8]=arr['G'];
 		arr['E']-=arr['G'];
 		arr['I']-=arr['G'];
 		arr['H']-=arr['G'];
 		arr['T']-=arr['G'];
 		arr['G']=0;
 		dig[3]=arr['H'];
 		arr['T']-=arr['H'];
 		arr['R']-=arr['H'];
 		arr['E']-=arr['H'];
 		arr['E']-=arr['H'];
 		arr['H']=0;
 		dig[2]=arr['T'];
 		arr['W']-=arr['T'];
 		arr['O']-=arr['T'];
 		arr['T']=0;
 		dig[7]=arr['S'];
 		arr['E']-=arr['S'];
 		arr['V']-=arr['S'];
 		arr['E']-=arr['S'];
 		arr['N']-=arr['S'];
 		arr['S']=0;
 		dig[5]=arr['V'];
 		arr['F']-=arr['V'];
 		arr['I']-=arr['V'];
 		arr['E']-=arr['V'];
 		arr['V']=0;
 		dig[4]=arr['F'];
 		arr['O']-=arr['F'];
 		arr['U']-=arr['F'];
 		arr['R']-=arr['R'];
 		arr['F']=0;
 		dig[1]=arr['O'];
 		dig[9]=arr['E']-dig[1];
 		cout<<"Case #"<<k+1<<": ";
 		for (int i = 0; i < 10; ++i)
 		{
 			for (int j = 0; j < dig[i]; ++j)
 			{
 				cout<<i;
 			}
 		}
 		cout<<endl;
 	}
 	return 0;
 }
#include<iostream>
 //#include<string.h>
 //#include<cstdio>
 //#include<algorithm>
 #include<string>
 using namespace std;
 
 int main()
 {
 	int T;
 	cin>>T;
 	
 	for(int i=1,N;i<=T;i++)
 	{
 		//cin>>N;
 		string s="",s1="";
 		cin>>s;
 		int l=s.size();
 		s1=s[0];
 		for(int j=1;j<l;j++)
 		{
 			if(s1[0]>s[j]) s1=s1+s[j];
 			else s1=s[j]+s1;
 		} 
 		cout<<"Case #"<<i<<": "<<s1<<endl;
 		//reinitialize here			
 	}
 	
 	return 0;
 }

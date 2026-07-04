#include<iostream>
 #include<map>
 #include<cstdio>
 using namespace std;
 
 int main()
 {
 	int t;
 	cin>>t;
 	for( int q = 0; q<t; q++ )
 	{
 		long long int i;
 		cin>>i;
 		long long int  sum = 0;
 		long long int limit = i*100;
 		map<int,int>m;
 		int f = 0;
 		for( sum = i; sum < limit; sum += i)
 		{
 			long long int temp = sum;
 			while( temp )
 			{
 				m[ temp%10 ] = 1;
 				temp = temp/10;
 			};
 			int count = 0;
 			for( int j = 0; j<10; j++)
 			{
 				if(m[j]) count++;
 			}
 			if(count==10) 
 			{
 				f=1;				
 				break;			
 			}
 		}
 		if(!f)
 			cout<<"Case #"<<q+1<<": "<<"INSOMNIA"<<"\n";
 		else
 			cout<<"Case #"<<q+1<<": "<<sum<<"\n";
 	}
 }

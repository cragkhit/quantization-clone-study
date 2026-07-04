#include <iostream>
 #include <iomanip>
 #include <vector>
 #include <cmath>
 #include <queue>
 #include <bitset>
 #include <algorithm>
 #include <string>
 #include <sstream> 
 
 using namespace std;
 
 /*int div2 (vector<int> in)
 {
 	int out = 0;
 	while(true)
 	{
 		for (int i = 0; i < in.size()-1; i++)
 		{
 			if (in[i]%2 == 1)
 				in[i+1] += 10;
 			in[i]/=2;
 		}
 		if (in[in.size()] == 1)
 			return out;
 		else in[in.size()] /= 2;
 	}
 }*/
 
 unsigned long long gcd (unsigned long long a, unsigned long long b)
 {
 	if (b == 0)
 		return a;
 	return gcd(b, a%b);
 }
 
 int main()
 {
   int T;
   int x = 0;
   cin>>T;
   while(++x&&(T-x+1))
     {
       cout<<"CASE #"<<x<<": ";
 	  unsigned long long P,Q;
 	  cin>>P;
 	  cin.ignore(1,'\\');
 	  cin>>Q;
 
 	  unsigned long long g = gcd(P,Q);
 	  P/=g;
 	  Q/=g;
 
 	  int power = 0;
 	  unsigned long long temp = 1;
 	  while (temp < Q)
 	  {
 		  temp*=2;
 		  power++;
 		  if (temp == Q)
 		  {
 			  break;
 		  }
 	  }
 	  if (temp != Q)
 		  cout<<"impossible"<<endl;
 	  else
 	  {
 		  int sep = 0;
 		  while (temp > P)
 		  {
 			  sep++;
 			  temp/=2;
 		  }
 		  cout<<sep<<endl;
 	  }
     }
   return 0;
 }
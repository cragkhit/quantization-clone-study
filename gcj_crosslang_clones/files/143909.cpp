#include <bits/stdc++.h>
 using namespace std;
 #define FOR(i,n) for(int i=0;i<n;++i)
 
 
 bool add(int x,int &mask) {
 	if(x==0) {
 		mask|=1;
 		return mask==(1<<10)-1;
 	}
 	while(x) {
 		mask|=1<<(x%10);
 		x/=10;
 	}
 	return mask==(1<<10)-1;
 }
 		
 
 int main(int argc, char** argv) {
 	int tt;
 	scanf("%d",&tt);
 	for(int cc=1;cc<=tt;++cc) {
 		int n;
 		scanf("%d",&n);
 		if(n==0) {
 			printf("Case #%d: INSOMNIA\n",cc);
 			continue;
 		}
 		int mask = 0;
 		int t;
 		for(t=n;!add(t,mask);t+=n);
 		printf("Case #%d: %d\n",cc,t);
 		
 	}
 	
 }
#include <stdio.h>
 #include <stdlib.h>
 #include <string.h>
 #include <math.h>
 #include <vector>
 #include <map>
 #include <utility>
 #include <set>
 #include <algorithm>
 #include <iostream>
 #include <sstream>
 
 using namespace std;
 
 #define CLEAR(X) memset(X,0,sizeof(X))
 #define REP(i,n) for(int i=0;i<(n);i++) 
 template <class T> vector<T>parse(string s,const char d=' '){
   vector<T> v; string p; s+=d; int i=0; 
   while(i<(int)s.size())
     if (s[i] == d){stringstream u; u<<p; T t; u>>t; v.push_back(t); p=""; while(i<(int)s.size() && s[i]==d)i++;} else p+=s[i++];   
   return v;
 } 
 
 typedef long long ll;
 typedef long double ld;
 
 int main(){
   int _cases; scanf("%d",&_cases);
   for(int _case=1;_case<=_cases;_case++){
     printf("Case #%d:",_case);
     int r,c;scanf("%d%d",&r,&c);
     int a[111][111];
     REP(i,r)REP(j,c)scanf("%d",&a[i][j]);
 
     bool ok = true;
     REP(i,r)REP(j,c) {
       bool rows = true, cols = true;
       REP(k,c) if (a[i][k] > a[i][j]) rows = false;
       REP(k,r) if (a[k][j] > a[i][j]) cols = false;
       if (!rows && !cols) ok = false;
     }
     if(ok)printf(" YES\n");
     else printf(" NO\n");
 
   }
   return 0;
 }

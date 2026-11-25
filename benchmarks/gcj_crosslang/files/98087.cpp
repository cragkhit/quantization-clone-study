#include<cstdio>
 #include<cstring>
 #include<cstdlib>
 #include<cctype>
 #include<cmath>
 #include<iostream>
 #include<fstream>
 #include<numeric>
 #include<string>
 #include<vector>
 #include<queue>
 #include<map>
 #include<algorithm>
 #include<set>
 #include<sstream>
 #include<stack>
 #include<list>
 #include<iterator>
 
 using namespace std;
 
 #define Limit 100005
 #define FRO freopen("in.txt","r",stdin);
 #define FRU freopen("out.txt","w",stdout);
 
 #define SET(a) memset(a,-1,sizeof(a))
 #define CLR(a) memset(a,0,sizeof(a))
 #define i64 long long
 //#deinfe i64 __int64
 #define pb push_back
 #define mp make_pair
 #define ff first
 #define ss second
 #define infinity 2147483647
 #define pi acos(-1.0)
 #define eps 1e-9
 
 
 
 #define foreach(i,n) for(__typeof((n).begin())i =(n).begin();i!=(n).end();i++)
 
 
 template< class T > T sqr(T n) { return n*n; }
 template< class T > T gcd(T a, T b) { return (b != 0 ? gcd<T>(b, a%b) : a); }
 template< class T > T lcm(T a, T b) { return (a / gcd<T>(a, b) * b); }
 template< class T > T Max(T a, T b) { return a>b?a:b; }
 template< class T > T Min(T a, T b) { return a<b?a:b; }
 template< class T > T abs(T a) { return a>0?a:-a; }
 
 //const int row[]={-1, -1, -1,  0,  0,  1,  1,  1};  // Kings Move
 //const int col[]={-1,  0,  1, -1,  1, -1,  0,  1};  // Kings Move
 //const int row[]={-2, -2, -1, -1,  1,  1,  2,  2};  // Knights Move
 //const int col[]={-1,  1, -2,  2, -2,  2, -1,  1};  // Knights Move
 //const int row[]={-1,0,0,1,0};
 //const int col[]={0,-1,1,0,0};
 
 int n;
 char b[10][10],d[10][10];
 
 
 
 int main()
 {
     //FRO
     //FRU
     int tc,t,i,j,k,a,c;
 
     cin>>tc;
     for(t=1;t<=tc;t++)
     {
         for(i=0;i<4;i++)
         {
             cin>>b[i];
             strcpy(d[i],b[i]);
         }
         for(i=0;i<4;i++)
         {
             for(j=0;j<4;j++)if(b[i][j]=='T')b[i][j]='X',d[i][j]='O';
         }
 
         int x,o,inc;
         char tm;
         x=o=inc=0;
         for(i=0;i<4;i++)
         {
             tm='X';
             if(b[i][0]==tm && b[i][1]==tm && b[i][2]==tm && b[i][3]==tm)x=1;
             else if(b[0][i]==tm && b[1][i]==tm && b[2][i]==tm && b[3][i]==tm)x=1;
             tm='O';
             if(d[i][0]==tm && d[i][1]==tm && d[i][2]==tm && d[i][3]==tm)o=1;
             else if(d[0][i]==tm && d[1][i]==tm && d[2][i]==tm && d[3][i]==tm)o=1;
         }
         tm='X';
         if(b[0][0]==tm && b[1][1]==tm && b[2][2]==tm && b[3][3]==tm)x=1;
         else if( b[0][3]==tm && b[1][2]==tm && b[2][1]==tm && b[3][0]==tm )x=1;
         tm='O';
         if(d[0][0]==tm && d[1][1]==tm && d[2][2]==tm && d[3][3]==tm)o=1;
         else if( d[0][3]==tm && d[1][2]==tm && d[2][1]==tm && d[3][0]==tm )o=1;
 
         for(i=0;i<4;i++)for(j=0;j<4;j++)if(b[i][j]=='.')inc=1;
 
         cout<<"Case #"<<t<<": ";
         if(x)cout<<"X won";
         else if(o)cout<<"O won";
         else if(inc)cout<<"Game has not completed";
         else cout<<"Draw";
         cout<<endl;
     }
 
 
     return 0;
 }
 

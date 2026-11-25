#include <iostream>
 #include <map>
 #include <vector>
 #include <string>
 #include <set>
 #include <queue>
 
 using namespace std;
 
 #define forn(i, n) for(int i = 0; i < (n); ++i)
 
 int64_t solve(int64_t n) {
   int mask = 0;
   for (int64_t x = n; ; x += n) {
     int64_t y = x;
     while (y) {
       mask |= 1 << (y % 10);
       y /= 10;
     }
     // cout << x << endl;
     if (mask == (1 << 10) - 1) {
       return x;
     }
   }
   // cout << "overflow" << endl;
   return -1;
 }
 
 void solve() {
   int n;
   cin >> n;
   if (n == 0) {
     cout << "INSOMNIA" << endl;
     return;
   }  
   cout << solve(n) << endl;
 }
 
 int main() {
   // freopen("in.txt", "r", stdin);
   freopen("A-large.in.txt", "r", stdin);
   freopen("out.txt", "w", stdout);
   ios_base::sync_with_stdio(false);
   int t;
   cin >> t;
   for (int i = 0; i < t; ++i) {
     cout << "CASE #" << i + 1 << ": ";
     solve();
   }
 
   return 0;
 }
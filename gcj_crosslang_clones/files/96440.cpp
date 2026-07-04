#include <list>
 #include <map>
 #include <set>
 #include <stack>
 #include <queue>
 #include <algorithm>
 #include <sstream>
 #include <iostream>
 #include <cstdio>
 #include <cmath>
 #include <cstdlib>
 #include <cstring>
 #include <climits>
 #include <cfloat>
 #include <numeric>
 #include <iomanip>
 #include <cassert>
 
 using namespace std;
 
 typedef long long ll;
 typedef unsigned long long ull;
 typedef vector<int> vi;
 typedef vector<string> vs;
 typedef pair<int, int> pii;
 
 const int oo = 0x3f3f3f3f;
 const double eps = 1e-9;
 
 #define sz(c) int((c).size())
 #define all(c) (c).begin(), (c).end()
 #define FOR(i,a,b) for (int i = (a); i < (b); i++)
 #define FORD(i,a,b) for (int i = int(b)-1; i >= (a); i--)
 #define FORIT(i,c) for(__typeof__((c).begin()) i=(c).begin(); i!=(c).end(); i++)
 #define mp make_pair
 #define pb push_back
 
 int main() {
 	int tc;
 	cin >> tc;
 	FOR(t, 0, tc) {
 		int d;
 		cin >> d;
 		vi v(d);
 		int ma = 0;
 		FOR(i, 0, d) {
 			cin >> v[i];
 			ma = max(ma, v[i]);
 		}
 		int best = ma;
 		FOR(i, 1, ma) {
 			int cur = 0;
 			FOR(j, 0, d) {
 				int p = v[j] / i;
 				if (v[j] % i == 0) {
 					p--;
 				}
 				cur += p;
 			}
 			best = min(best, cur + i);
 		}
 		cout << "Case #" << t+1 << ": " << best << endl;
 	}
 
 	return 0;
 }

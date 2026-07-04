#include <cstdio>
 #include <cstring>
 #include <cmath>
 #include <iostream>
 #include <string>
 #include <set>
 #include <map>
 #include <vector>
 #include <algorithm>
 
 using namespace std;
 
 #define debug 1
 
 #define cerr if(debug) cerr
 
 #define For(i, a, b) for(int i = a; i < b; i++)
 #define sz(a) ((int)a.size())
 #define mp make_pair
 #define pb push_back
 #define all(a) a.begin(), a.end()
 
 typedef pair<int, int> pii;
 typedef long long lint;
 
 const int inf = 0x7fffffff;
 const int Size = 1000 * 1000 + 1;
 char buffer[Size];
 
 const char one = 0;
 const char I = 1;
 const char J = 2;
 const char K = 3;
 const char mOne = 4;
 const char mI = 5;
 const char mJ = 6;
 const char mK = 7;
 
 const int size = 10 * 1000 + 1;
 char ar[size];
 
 /*const int table[4][4] = {{one, I, J, K},
 	             {I, -mOne, K, -mJ},
 	             {J, -mK, -mOne, I},
 	             {K, J, -mI, -mOne}};
 				 */
 
 const char table[8][8] = {
 				 {one, I, J, K, mOne, mI, mJ, mK},
 	             {I, mOne, K, mJ, mI, one, mK, J},
 	             {J, mK, mOne, I, mJ, K, one, mI},
 	             {K, J, mI, mOne, mK, mJ, I, one},
 
 				 {mOne, mI, mJ, mK, one, I, J, K},
 	             {mI, one, mK, J, I, mOne, K, mJ},
 	             {mJ, K, one, mI, J, mK, mOne, I},
 	             {mK, mJ, I, one, K, J, mI, mOne}
 };
 
 inline char power(char a, lint p) {
 	char res = one;
 	while(p) {
 		if(p & 1) {
 			res = table[res][a];
 		}
 		a = table[a][a];
 		p >>= 1;
 	}
 
 	return res;
 }
 
 
 void init() {
 
 }
 
 void clear(int i) {
 
 }
 
 char stringPref[size + 1];
 
 string toString(char c) {
 	switch(c) {
 		case one:
 			return "1";
 		case I:
 			return "i";
 		case J:
 			return "j";
 		case K:
 			return "k";
 		case mOne:
 			return "-1";
 		case mI:
 			return "-i";
 		case mJ:
 			return "-j";
 		case mK:
 			return "-k";
 	}
 	return "wrong";
 }
 
 
 
 int solution(int nTest) {
 	//cerr << "cerr test " << nTest + 1 << endl;
 	int l;
 	lint x;
 	cin >> l >> x;
 	scanf("%s", buffer);
 	For(i, 0, l) {
 		if(buffer[i] == 'i') {
 			ar[i] = I;
 		} else if(buffer[i] == 'j') {
 			ar[i] = J;
 		} else {
 			ar[i] = K;
 		}
 	}
 	stringPref[0] = one;
 	For(i, 0, l) {
 		stringPref[i + 1] = table[stringPref[i]][ar[i]];
 	//	cerr << toString(stringPref[i + 1]);
 	}
 	cerr << endl;
 
 	char curPref = one;
 	int indPref = 0;
 	lint start = 1;
 	int sucI = 0;
 	for(lint lp = 1; lp <= 10 * l; lp++) {
 		lint lt = lp - 1;
 		if(lt >= l * x) {
 			break;
 		}
 		curPref = table[curPref][ar[indPref]];
 		char middleStart = one;
 		for(int i = indPref + 1; i < l; i++) {
 			middleStart = table[middleStart][ar[i]];
 		}
 
 		char curSuf = one;
 		int indSuf = l - 1;
 		lint end = x - 1;
 	//	cerr << "(" << lt << "," << toString(curPref) << ")" << endl;
 
 		if(curPref == I) {
 			sucI++;
 			if(sucI >= 2 * l) {
 				break;
 			}
 			int sucK = 0;
 			for(lint rp = 1; rp < 10 * l; rp++) {
 
 				lint rt = l * x - rp;
 				if(rt <= lt) {
 					break;
 				}
 				curSuf = table[ar[indSuf]][curSuf];
 				char middleEnd = stringPref[indSuf];
 				
 				lint p = end - start;
 				char middle = one;
 		//		cerr << "power" << p << endl;
 				if(curSuf == K) {
 					sucK++;
 					if(sucK >= 2 * l) {
 						break;
 					}
 					if(p >= 0) {
 						char middleMiddle = power(stringPref[l], p);
 
 						middle = table[middleStart][middleMiddle];
 						middle = table[middle][middleEnd];
 			//			cerr << "0|" <<  toString(middleStart) << "," << toString(middleMiddle) << "," << toString(middleEnd) << endl;
 					} else if(p == -1) {
 						for(int i = indPref + 1; i < indSuf; i++) {
 							middle = table[middle][ar[i]];
 						}
 			//			cerr << "-1|" <<  toString(middle) << endl;
 					} else {
 						throw "exc";
 					}
 			//		cerr << "(" << lt << "," << rt << ")(" << toString(curPref) << "," << toString(middle) << "," << toString(curSuf) << ")" << endl;
 					if(curPref == I && middle == J && curSuf == K) {
 						puts("YES");
 						return 0;
 					}
 				}
 
 
 				indSuf--;
 				if(indSuf == -1) {
 					indSuf = l - 1;
 					end--;
 				}
 			}
 		}
 		indPref++;
 		if(indPref == l) {
 			indPref = 0;
 			start++;
 		}
 	}
 	puts("NO");
 
 	return 0;
 }
 
 int main() {
 	freopen("input.txt", "r", stdin);
 	freopen("output.txt", "w", stdout);
 
 	int i = 0, n = 999999;
 	scanf("%d", &n);
 	init();
 	For(i, 0, n) {
 		cerr << i << endl;
 		printf("Case #%d: ", i + 1);
 		clear(i);
 		solution(i);
 	}
 
 	return 0;
 }
 	

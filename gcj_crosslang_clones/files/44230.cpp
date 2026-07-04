#include <bits/stdc++.h>
 
 using namespace std;
 
 #define forless(i, s, e) for(int i = s; i < e; ++i)
 #define forto(i, s, e) for(int i = s; i <= e; ++i)
 
 int main(){
 	int T;
 	cin >> T;
 	forless(t, 0, T){
 		int sMax;
 		string shyness;
 		cin >> sMax;
 		cin >> shyness;
 		int inviteCnt = 0;
 		int upCnt = shyness[0] - '0';
 		forto(i, 1, sMax){
 			if(shyness[i] > '0'){
 				if(upCnt < i){
 					inviteCnt += i - upCnt;
 					upCnt = i;
 				}
 				upCnt += shyness[i] - '0';
 			}
 		}
 		printf("Case #%d: %d\n", t + 1, inviteCnt);
 	}
 	return 0;
 }

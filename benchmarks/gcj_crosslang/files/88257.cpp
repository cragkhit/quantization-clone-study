#include <bits/stdc++.h>
 using namespace std;
 int input[1005];
 int main() {
 	int t, cnt = 1;
 	scanf("%d", &t);
 	while(t--) {
 		int d;
 		scanf("%d", &d);
 		int i;
 		int maxn = INT_MIN;
 		int hash[1005] = {0};
 		for(i = 0; i < d; i++) {
 			int x;
 			scanf("%d", &x);
 			input[i] = x;
 		}
 		int ans = 1000000000;
 		for(i = 1; i <= 1000; i++) {
 			int sum = 0;
 			int max = INT_MIN;
 			int divisions = 0;
 			int j;
 			for(j = 0; j < d; j++) {
 				if(input[j] > i) {
 					if(input[j] % i == 0) {
 						divisions += input[j] / i - 1;
 					} else {
 						divisions += input[j] / i;
 					}
 					max = i;
 				} else {
 					if(max < input[j]) {
 						max = input[j];
 					}
 				}
 			}
 			if(ans > max + divisions) {
 				ans = max + divisions;
 			}
 		}
 		printf("Case #%d: %d\n", cnt++, ans);
 	}
 	return 0;
 }
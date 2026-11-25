#include <stdio.h>
 #include <algorithm>
 using namespace std;
 
 int g[200][200];
 
 bool solve(int R, int C)
 {
 	for(int r = 0; r < R; r++)
 	{
 		for(int c = 0; c < C; c++)
 		{
 			int maxR = 0;
 			for(int rr = 0; rr < R; rr++)
 				maxR = max(maxR, g[rr][c]);
 			int maxC = 0;
 			for(int cc = 0; cc < C; cc++)
 				maxC = max(maxC, g[r][cc]);
 				
 			if(maxC > g[r][c] && maxR > g[r][c])
 				return false;
 		}
 	}
 	
 	return true;
 }
 
 int main()
 {
 	int T;
 	scanf("%d", &T);
 	
 	for(int t = 0; t < T; t++)
 	{
 		int R, C;
 		scanf("%d %d", &R, &C);
 		for(int r = 0; r < R; r++)
 			for(int c = 0; c < C; c++)
 				scanf("%d", &g[r][c]);
 		bool b = solve(R, C);
 		if(b)
 			printf("Case #%d: YES\n", t + 1);
 		else
 			printf("Case #%d: NO\n", t + 1);
 	}
 	
 	
 	return 0;
 }

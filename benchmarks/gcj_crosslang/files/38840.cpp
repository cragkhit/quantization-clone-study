#include <cstdio>
 
 #define D 1000
 
 int T,d,p[D+9];
 int ans,tmp;
 
 int main()
 {
 	scanf("%d",&T);
 	for (int t=1;t<=T;++t)
 	{
 		scanf("%d",&d);
 		int max_p=1;
 		for (int i=1;i<=d;++i)
 		{
 			scanf("%d",p+i);
 			if (max_p<p[i]) max_p=p[i];
 		}
 		ans=1009;
 		for (int j=1;j<=max_p;++j)
 		{
 			tmp=j;
 			for (int i=1;i<=d;++i) tmp+=(p[i]+j-1)/j-1;
 			if (ans>tmp) ans=tmp;
 		}
 		printf("Case #%d: %d\n",t,ans);
 	}
 	return 0;
 }

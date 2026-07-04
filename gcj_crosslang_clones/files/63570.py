#! /usr/bin/env python2.7
 
 T=int(raw_input())
 for test in range(1,T+1):
 	Line=raw_input().split()
 	K=int(Line[0])
 	C=int(Line[1])
 	S=int(Line[2])
 		
 	if C*S<K :
 		print "Case #{}: IMPOSSIBLE".format(test)
 	else :
 		Choices=[]		
 		j=0
 		pw=range(0, C)
 		pw.reverse()
 		for i in range(0,K//C):
 			N=0
 			for p in pw:
 				N+=j*K**p
 				j+=1
 			Choices.append(N+1)
 		if K % C <> 0:
 			N=0
 			i=0
 			while j<K:
 				N+=j*K**pw[i]
 				i+=1
 				j+=1
 			Choices.append(N+1)
 			
 		print "Case #{}: ".format(test)+" ".join([str(N) for N in Choices])
import sys
 import numpy as np
 
 def solve(smax,slist):
 	#print smax
 	#print slist
 	cumslist = np.cumsum(slist)
 	delta = -cumslist+range(smax+1)+1
 	return max([max(delta),0])
 
 f = open(sys.argv[1],"r")
 T = int(f.readline())
 
 for row in range(T):
 	myline = f.readline().split(" ")
 	smax = int(myline[0])
 	slist = np.array([int(x) for x in list(myline[1].rstrip())])
 	print "Case #%d: %s"%(row+1,solve(smax,slist))
 f.close()

import sys
 import math
 
 thickness = 1 #ring thickness in cm
 
 def main():
 	f = open(sys.argv[1])
 	T = int(f.readline().strip("\n"))
 	for i in range (0,T):
 		print "Case #%d: %d" % (i+1, findRings(f))
 
 def findRings(f):
 	r, t = [int(s) for s in f.readline().strip().split()]
 	#print r, t
 	f_o = 2*r+1
 	b = f_o-2
 	a = 2
 	c = -t
 	rings = math.floor((-b + math.sqrt(b*b - 4*a*c))/(2*a))
 #	print rings
 	value = 2*rings*rings + (f_o-2)*rings - t
 #	print value
 	if value > 0:
 		rings = checkDown(rings,f_o,t)
 	elif value < 0:
 		rings = checkUp(rings,f_o,t)
 	else: pass
 	return rings
 
 def checkDown(x, f, t):
 	value = 2*x*x + (f-2)*x - t
 	rings = x
 	if value > 0:
 		rings = checkDown(x-1,f,t)
 	return rings
 
 def checkUp(x,f,t):
 	value = 2*x*x + (f-2)*x - t
 	rings = x
 	if value == 0: return rings
 	if value > 0:
 		return rings-1
 	else:
 		rings = checkUp(x+1,f,t)
 	return rings
 
 
 if __name__ == "__main__":
 	main()
 

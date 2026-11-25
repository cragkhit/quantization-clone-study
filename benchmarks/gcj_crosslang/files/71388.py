import sys
 
 T = int( raw_input() )
 for j in range(1,T+1):
 	unused = [ '0','1','2','3','4','5','6','7','8','9' ]
 	n = raw_input()
 	i=1
 	p = n
 	if n == '0':
 		print "Case #{}: INSOMNIA".format( j )
 		continue
 	while unused:
 		p = str( int (n ) * i )
 		i += 1
 		for ch in p:
 			if ch in unused:
 				unused.remove( ch )
 	print  "Case #{}: {}".format( j, p )
 	
#!/usr/bin/env python
 
 import sys
 
 def solve (casenum, board, m, n):
 	result = "YES"
 	rowmax = map (max, board)
 	colmax = [max (map (lambda r: r[c], board)) for c in xrange (n)]
 #	print rowmax[0]
 #	print colmax[0]
 
 	try:
 		for r in xrange (m):
 			for c in xrange (n):
 				#print "checking ", board[r][c], " against ", rowmax[r], colmax[c]
 				if (board[r][c] < rowmax[r]) \
 				and (board[r][c] < colmax[c]):
 					result = "NO"
 					#print "no for ", r, c
 					raise StopIteration
 
 	except StopIteration:
 		pass
 
 	print "Case #%d: %s" % (casenum, result)
 
 
 num_testcases = int (sys.stdin.readline())
 
 for case in range (1, num_testcases+1):
 	(m, n) = map (int, sys.stdin.readline().split())
 	lawn = [0 for x in xrange (m)]
 
 	for row in xrange (m):
 		lawn[row] = map (int, sys.stdin.readline().split())
 
 	#print lawn, "\n\n"
 	solve (case, lawn, m, n)

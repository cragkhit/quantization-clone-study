#!/usr/bin/python
 
 import sys
 
 def get_status(map):
 	if is_winner(map, 'X'):
 		return "X won"
 	elif is_winner(map, 'O'):
 		return "O won"
 	elif not_completed_yet(map):
 		return "Game has not completed"
 	else:
 		return "Draw"	
 
 def is_winner(state, l):
 	skip_column = set()
 	
 
 	for r, row in enumerate(state):
 		for c in range(len(row)):
 			if row[c] == l or row[c] == 'T':
 				if c == 3:
 					return True				
 			else:
 				skip_column.add(c)
 				break 	
 					
 	for c in range(4):
 		if c not in skip_column:
 			for r in range(4):
 				if state[r][c] == l or state[r][c] == 'T':
 					if r == 3:
 						return True
 				else:
 					 break		
 
 	for i in range(4):
 		if state[i][i] == l or state[i][i] == 'T':
 			if i == 3:
 				return True
 		else:
 			break
 
 	for i in range(4):
 		if state[i][4-i-1] == l or state[i][4-i-1] == 'T':
 			if i == 3:
                                 return True
 		else:
 			break
 
 	return False
 
 def not_completed_yet(state):
 	return next((True for i, sublist in enumerate(state) if "." in sublist), False)
 
 f = open(sys.argv[1], 'r')
 
 num_of_cases = int(f.readline())
 game_counter = 1 
 
 map = []
 
 for line in f:
 	if line.strip() == '':
 		print "Case #%i: %s" % (game_counter, get_status(map))
 		map = []
 		game_counter += 1
 		continue
 	
 	map.append(list(line.strip()))

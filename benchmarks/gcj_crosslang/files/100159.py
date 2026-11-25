import math
 
 def formatLine(line):
 	s = line.split()
 	s = [int(i) for i in s]
 
 	return s
 
 def solveTestCase(plates):
 	plates = formatLine(plates)
 	plates.sort()
 	highest = max(plates)
 
 	moves = [0 for j in range(1000)]
 
 
 	for ind,a in enumerate(moves):
 		a = [0 for j in range(len(plates))]
 		for ind2,i in enumerate(a):
 			if plates[ind2] > (ind+1):
 				a[ind2] = int(math.ceil(float(plates[ind2])/(ind+1)))-1
 			else:
 				a[ind2] = 0
 
 		moves[ind] = sum(a) + min(highest, ind+1)
 
 	return min(highest, min(moves))
 
 
 
 
 
 
 data = open("B-large.in", 'r')
 out = open("answer.txt", "w")
 numberCases = int(data.readline())
 
 for i in range(numberCases):
 	numdiners = data.readline()
 	plates = data.readline()
 	s = "Case #{0:d}: {1:d}\n".format(i+1, solveTestCase(plates))
 	print s
 	out.write(s)
 

with open('out', 'w') as out:	
 	fairsquares = list()
 	for each in range(0,10000000):
 		x = each * each
 		y = str(each)
 		z = str(x)
 		if (z == z[::-1]) and (y == y[::-1]):
 			fairsquares.append(x)
 		length = len(fairsquares)
 	with open('C-large-1.in', 'r') as data:
 		lines = data.readline()
 		lines = int(lines)
 		for all in range(lines):
 			counter = 0
 			temp = data.readline()
 			(lower, upper) = temp.split(' ', 1)
 			lower = int(lower)
 			upper = int(upper)
 			for each in fairsquares:
 				if each > upper:
 					break
 				if each >= lower:
 					counter = counter + 1
 			out.write('Case #' + str(all+1) + ': '+ str(counter) + '\n')
#!/usr/bin/env python3
 
 import os
 import sys
 
 file = open("input", "r")
 
 cases = file.readline()
 
 for line_nr in range(1,int(cases)+1):
 	line = file.readline()
 	Smax = line.split(" ")[0]
 	data = line.split(" ")[1]
 
 	stand=int(data[0])
 	friends=0
 
 	for i in range(1,len(data[1:])):
 		if data[i]=="0":
 			continue
 
 		if stand<i:
 			friends += i-stand
 		
 		stand += int(data[i]) + friends
 
 	print("Case #" + str(line_nr) + ": " + str(friends))
 
 file.close()

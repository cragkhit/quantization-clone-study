filename = raw_input('Enter the filename: ')
 
 #################
 # Timing
 import time
 start = time.time()
 
 # Opening file
 defaultfn = 'a-init.in'
 try:
   if len(filename) == 0:
     fi = open(defaultfn)
     filename = defaultfn
   else:
     fi = open(filename)
 except:
   print "File cannot be opened", filename
   exit()
 
 #################
 # Solution.
 testCases = int(fi.readline())
 results = list()
 
 for x in xrange(0, testCases):
   # New case
   casetime = time.time()
   case = str(x+1)
   caseLine = "Case #" + case
   print caseLine
   s = fi.readline().rstrip()
 
   tmp = list()
   
   # tmp.append(line[0])
   # for letter in line[1:]:
   #   tmpNext = list()
   #   for variation in tmp:
   #     if (variation[0] <= letter):
   #       tmpNext.append(letter + variation)
   #     else:
   #       tmpNext.append(variation + letter)
   #   tmp = tmpNext
   while ("Z" in s):
     s = s.replace("Z", "", 1).replace("E", "", 1).replace("R", "", 1).replace("O", "", 1)
     tmp.append(0)
   while ("W" in s):
     s = s.replace("T", "", 1).replace("W", "", 1).replace("O", "", 1)
     tmp.append(2)
   while (("G" in s) and ("H" in s)):
     s = s.replace("E", "", 1).replace("I", "", 1).replace("G", "", 1).replace("H", "", 1).replace("T", "", 1)
     tmp.append(8)
   while ("U" in s):
     s = s.replace("F", "", 1).replace("O", "", 1).replace("U", "", 1).replace("R", "", 1)
     tmp.append(4)
   while ("X" in s):
     s = s.replace("S", "", 1).replace("I", "", 1).replace("X", "", 1)
     tmp.append(6)
   while ("R" in s):
     s = s.replace("T", "", 1).replace("H", "", 1).replace("R", "", 1).replace("E", "", 2)
     tmp.append(3)
   while ("O" in s):
     s = s.replace("O", "", 1).replace("N", "", 1).replace("E", "", 1)
     tmp.append(1)
   while ("S" in s):
     s = s.replace("S", "", 1).replace("E", "", 2).replace("V", "", 1).replace("N", "", 1)
     tmp.append(7)
   while ("V" in s):
     s = s.replace("F", "", 1).replace("I", "", 1).replace("V", "", 1).replace("E", "", 1)
     tmp.append(5)
   while ("N" in s):
     s = s.replace("N", "", 2).replace("I", "", 1).replace("E", "", 1)
     tmp.append(9)
 
   print tmp
   tmp.sort()
   results.append(caseLine + ": " + ''.join(map(str, tmp)))
   # exit()
 
   print "Case time:", str(time.time() - casetime), "sec"
   
 
 print '\n'.join(results)
 
 #################
 # Output file.
 name = filename.split('.')[0]
 fo = open(name + '.out', 'w')
 fo.write('\n'.join(results))
 
 fi.close()
 fo.close()
 
 print "Total time:", str(time.time() - start), "sec"
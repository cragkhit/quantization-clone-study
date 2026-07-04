inputName = 'A-large'
 
 # Take input
 infile = open(inputName + '.in', 'r')
 lines = infile.readlines()
 infile.close()
 
 outfile = open(inputName + '-out.txt', 'w')
 
 for x, line in enumerate(lines):
     if x == 0:
         numTests = int(line)
     else:
         letterDict = {
             'Z': 0,
             'O': 0,
             'W': 0,
             'T': 0,
             'U': 0,
             'V': 0,
             'X': 0,
             'S': 0,
             'G': 0,
             'I': 0,
         }
         result = []
         line=line.strip() # remove newline char
 
         # Count letters in string
         for char in line:
             if char in letterDict:
                 letterDict[char] += 1
 
         # print(letterDict)
 
         # Find count of letters unique to a number
 
         # Round 1,
         # remove (Z)ero,
         count = letterDict['Z']
         result.extend(['0']*count)
         letterDict['O'] -= count
 
         # T(w)o,
         count = letterDict['W']
         result.extend(['2']*count)
         letterDict['T'] -= count
         letterDict['O'] -= count
 
         # Si(x),
         count = letterDict['X']
         result.extend(['6']*count)
         letterDict['S'] -= count
         letterDict['I'] -= count
 
         # Ei(g)ht
         count = letterDict['G']
         result.extend(['8']*count)
         letterDict['T'] -= count
         letterDict['I'] -= count
 
         # Fo(u)r
         count = letterDict['U']
         result.extend(['4']*count)
         letterDict['O'] -= count
 
         # Second round - these numbers have unique letters after first round eliminated
 
         # (T)hree
         count = letterDict['T']
         result.extend(['3']*count)
 
         # (S)even
         count = letterDict['S']
         result.extend(['7']*count)
         letterDict['V'] -= count
 
         # Third round
         # (O)ne
         count = letterDict['O']
         result.extend(['1']*count)
 
         # Fi(v)e
         count = letterDict['V']
         result.extend(['5']*count)
         letterDict['I'] -= count
 
         # Fourth round
         count = letterDict['I']
         result.extend(['9']*count)
 
         result.sort()
         # print(result)
         result = ''.join(result)
         # Write output
         outfile.write('Case #' + str(x) + ': ' + str(result))
         if x < numTests:
             outfile.write('\n')
 
 outfile.close()

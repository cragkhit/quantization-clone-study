
 filePath = '/home/ashish/code/codejam/input.txt'
 outFilePath = '/home/ashish/code/codejam/output.txt'
 
 def openAndDump():
     f = open(filePath, 'r')
     o = open(outFilePath, 'wb')
     run(f, o)
 
 def run(f, o):
     cnt = -1
     for line in f:
         cnt = cnt + 1
         if cnt == 0:
             continue
         print 'line %s ' %line
         print 'Case #%s: %s\n' % (cnt, findNoOfFnSq(line))
         o.write('Case #%s: %s\n' % (cnt, findNoOfFnSq(line)))
 
 nos = (1, 4, 9, 121, 484,
     10201,
     12321,
     40804,
     44944,
     1002001,
     1234321,
     4008004,
     100020001,
     102030201,
     121242121,
     123454321,
     400080004,
     404090404,
     10000100001,
     10221412201,
     12102420121,
     12345654321,
     40000800004,
     1000001000001,
     1002003002001,
     1020304030201,
     1022325232201,
     1210024200121,
     1212225222121,
     1232345432321,
     1234567654321,
     4000008000004,
     4004009004004)
 
 def findNoOfFnSq(line):
     v = line.split(' ')
     s = int(v[0])
     e = int(v[1])
     cnt = 0
     for n in nos:
         if n > e:
             break
         elif n >= s and n <= v:
             cnt = cnt + 1
     return cnt
import sys
 import itertools
 import math
 import collections
 import functools
 
 def read_word(f):
     return next(f).strip()
 def read_int(f, b=10):
     return int(read_word(f), b)
 def read_letters(f):
     return list(read_word(f))
 def read_digits(f, b=10):
     return [int(x, b) for x in read_letters(f)]
 def read_words(f, d=' '):
     return read_word(f).split(d)
 def read_ints(f, b=10, d=' '):
     return [int(x, b) for x in read_words(f, d)]
 def read_floats(f, d=' '):
     return [float(x) for x in read_words(f, d)]
 def read_arr(f, R, reader=read_ints, *args, **kwargs):
     res = []
     for i in range(R):
         res.append(reader(f, *args, **kwargs))
     return res
 def solve(solver,SMALL=True,PRACTICE=False):
     fn = sys.argv[0]
     fn = fn[ 1+fn.rfind('/'): fn.rfind('.') ]
     fn = fn+['-large','-small'][SMALL]+['','-practice'][PRACTICE]
     in_fn = fn + '.in'
     out_fn = fn + '.out'
     with open(in_fn, 'r') as fi:
         with open(out_fn, 'w') as fo:
             T = read_int(fi)
             for i in range(T):
                 case = read_case(fi)
                 res = solver(case)
                 print i+1, res
                 write_case(fo, i+1, res)
 def write_case(f, i, res):
     f.write('Case #%d: '%i)
     f.write('%s'%res)
     f.write('\n')
 
 #######################################################
 
 def read_case(f):
     N = read_int(f)
     arr=  []
     for i in xrange(N):
         arr.append(read_word(f))
     return arr,N
 
 #######################################################
 
 def dist(x,y):
     #taxicab
     ans = 0
     for i in xrange(len(x)):
         ans += abs(x[i]-y[i])
     return ans
 def solver(case):
     arr,N = case
     crr = [] 
     for i in arr:
         brr = []
         prev = ''
         ct = 0
         for j in i:
             if j == prev:
                 ct += 1
             else:
                 brr.append( (prev,ct) )
                 #print 'brr!', brr
                 ct = 1
             prev = j
         brr.append((prev,ct))
         crr.append(brr[1:])
     check = map( lambda x: map(lambda y: y[0], x) , crr)
     c0 = check[0]
     for i in check:
         if i != c0: return "Fegla Won"
     dig = map( lambda x: map(lambda y: y[1], x) , crr)
     lend = len(dig[0])
     median = []
     lene = len(dig)
     for i in xrange(lend):
         median.append( int( round(sum( map(lambda x: x[i], dig) )/float(lene) ) ) )
     ans = 0
     for i in dig:
         ans += dist(median,i)
     return ans
     
 
 
     
 #False for large
 solve(solver,True)
 #solve(solver,False)
 
 print 'Done'

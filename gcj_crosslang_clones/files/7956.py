#!/usr/bin/python
 from sys import stdin, stderr
 
 def solve(cols):
   nums = {}
   for col in cols:
     for num in col:
       if num not in nums:
         nums[num] = 0
       nums[num] += 1
   result = []
   for num in nums:
     if nums[num] % 2 == 1:
       result.append(num)
   return ' '.join(str(x) for x in sorted(result))
 
 num_cases = int(stdin.readline())
 for case_num in range(num_cases):
   N = int(stdin.readline().strip())
   cols = []
   for _ in range(2 * N - 1):
     col  = [int(x) for x in stdin.readline().strip().split()]
     cols.append(col)
   result = 'Case #{0}: {1}'.format(case_num + 1, solve(cols))
   print result
   print >>stderr, result

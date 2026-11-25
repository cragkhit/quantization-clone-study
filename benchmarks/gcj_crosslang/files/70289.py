import argparse
 import os
 import sys
 
 def is_palidrome(value):
     for i in range(0, len(value)/2):
         if value[i] != value[-(i+1)]:
             return False
 
     return True
 
 def learn(limit):
     data = []
     for i in range(1, limit):
         print i
         if is_palidrome(str(i)):
             squared = i * i
             if is_palidrome(str(squared)):
                 data.append(squared)
 
     return data
 
 def solve(input_file, data):
     with open(input_file, 'rb') as f:
         n = int(f.readline())
         for i in range(0, n):
             a, b = f.readline().strip().split(' ')
             a = int(a)
             b = int(b)
 
             count = 0
             is_count = False
             for d in data:
                 if d > b:
                     break
                 if not is_count:
                     if a <= d:
                         is_count = True
                 if is_count:
                     count += 1
             print 'Case #%d: %s' % (i+1, count,)
 
 def main(args=sys.argv[1:]):
     parser = argparse.ArgumentParser()
     parser.add_argument('--learn', action='store_true', default=False)
     parser.add_argument('--limit', default=10000000, type=int)
     parser.add_argument('--data-file', default=os.path.join(os.getcwd(), 'data.py'))
     parser.add_argument('--solve', action='store_true', default=False)
     parser.add_argument('files', nargs='*')
     arguments = parser.parse_args(args)
 
     if arguments.learn:
         data = learn(arguments.limit)
         with open(arguments.data_file, 'wb') as f:
             f.write('DATA = ')
             f.write(str(data))
 
     if arguments.solve:
         globals0 = {}
         execfile(arguments.data_file, globals0)
         solve(arguments.files[0], globals0['DATA'])
 
 if __name__ == '__main__':
     sys.exit(main())

import sys
 
 def main(filename):
     with open(filename) as fin:
         nTests = int(fin.readline())
         test = 1
 
         while test <= nTests:
             board = []
             n, m = [int(i) for i in fin.readline().split()]
             row = 0
             while row < n:
                 board.append([int(i) for i in fin.readline().split()])
                 row += 1
             # board is ready
             possible = True
             row = 0
             while row < n:
                 col = 0
                 while col < m:
                     cell = board[row][col]
                     cell_rank = board[row]
                     cell_file = (board[r][col] for r in xrange(n))
                     if cell < max(cell_rank) and cell < max(cell_file):
                         possible = False
                         break
                     col += 1
                 if not possible:
                     print 'Case #{}: NO'.format(test)
                     break
                 row += 1
             if possible:
                 print 'Case #{}: YES'.format(test)
             test += 1
 
 
 if __name__ == '__main__':
     main(sys.argv[1])

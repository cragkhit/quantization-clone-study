#!/usr/bin/env python3
 # -*- coding: utf-8 -*-
 
 import collections, itertools, sys
 
 
 DIMENSION = 4
 (X_WON, O_WON, DRAW, NOT_ENDED) = ("X won", "O won", "Draw", "Game has not completed")
 
 
 def get_board_state(lines):
   state = NOT_ENDED
   diags = (tuple(lines[i][i] for i in range(DIMENSION)), tuple(lines[i][DIMENSION - 1 - i] for i in range(DIMENSION)))
   columns = tuple(tuple(lines[i][j] for i in range(DIMENSION)) for j in range(DIMENSION))
   for l in (lines + diags + columns):
     counts = collections.Counter(l)
     if (counts["X"] + counts["T"]) == DIMENSION:
       if state == O_WON:
         state = DRAW
         break
       else:
         state = X_WON
     elif (counts["O"] + counts["T"]) == DIMENSION:
       if state == X_WON:
         state = DRAW
         break
       else:
         state = O_WON
 
   if state == NOT_ENDED:
     try:
       empty_square = next(filter(".".__eq__, itertools.chain.from_iterable(lines)))
     except StopIteration:
       state = DRAW
   return state
 
 
 if __name__ == "__main__":
   input_filepath = sys.argv[1]
   output_filepath = "output.txt"
 
   with open(input_filepath, "rt") as input_file:
     T = int(next(input_file))
 
     for i in range(1, T + 1):
       lines = tuple(map(str.strip, itertools.takewhile("\n".__ne__, input_file)))
       print("Case #%d: %s" % (i, get_board_state(lines)))
 
     assert(i == T)

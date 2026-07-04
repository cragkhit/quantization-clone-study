#!/usr/bin/env python3
 # tttt.py - Tic-Tac-Toe-Tomek game state analyzer
 # Copyright 2013 Mansour <mansour@oxplot.com>
 
 # Game rules
 
 SIZE = 4
 PLAYERS = 'XO'
 TOMEK = 'T'
 TOMEK_S = set(TOMEK)
 EMPTY = '.'
 
 # Game states
 
 DRAW = 'DRAW'
 ONGOING = 'ONGOING'
 
 def get_game_state(game):
   ongoing = False
 
   rows = (set(r) - TOMEK_S for r in game)
   cols = (set(r[c] for r in game) - set(TOMEK_S) for c in range(SIZE))
   diag_tl = set(game[i][i] for i in range(SIZE)) - TOMEK_S
   diag_tr = set(game[i][-i - 1] for i in range(SIZE)) - TOMEK_S
 
   for ls in (rows, cols, (diag_tl, diag_tr)):
     for l in ls:
       if EMPTY in l:
         ongoing = True
         continue
       elif len(l) > 1:
         continue
       else:
         return next(iter(l))
 
   return ONGOING if ongoing else DRAW
 
 cnt = int(input())
 for i in range(cnt):
 
   game = []
   for j in range(SIZE):
     game.append(input())
   input()
 
   state = get_game_state(game)
 
   print('Case #%d: ' % (i + 1), end='')
   if state == DRAW:
     print('Draw')
   elif state == ONGOING:
     print('Game has not completed')
   else:
     print('%s won' % state)

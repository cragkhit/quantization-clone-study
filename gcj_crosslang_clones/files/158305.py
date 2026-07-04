from sys import stdin, stdout
 
 def solve_large(K, C, S):
     if S == K:
         return solve_small(K, C, S)
 
     if C == 1:
         return "IMPOSSIBLE"
 
     guessesRequired = (K + 1) / 2
     if S < guessesRequired:
         return "IMPOSSIBLE"
 
     guesses = []
     i = 0
     while len(guesses) < guessesRequired:
         branch = i * 2
         local = branch + 1
 
         #print local, branch, local + branch * K ** (C - 1)
 
         if (K % 2 == 1) and ((len(guesses) + 1) == guessesRequired):
             guesses.append(branch * K ** (C - 1))
         else:
             guesses.append(local + branch * K ** (C - 1))
 
         i += 1
 
     return " ".join([str(1 + g) for g in guesses])
 
 
 def solve_small(K, C, S):
     return " ".join([str(1 + i * K ** (C - 1)) for i in range(K)])
 
 
 T = int(stdin.readline())
 
 for t in range(T):
     K, C, S = map(int, stdin.readline().strip().split())
 
     result = solve_large(K, C, S)
 
     stdout.write("Case #%d: %s\n"%(t+1, result))
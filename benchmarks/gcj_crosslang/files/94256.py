def max_lw(x):
     return max([(i+1, x-1) for i in range(x/2+1)])
 
 def can_do(x, r, c):
     sz = r * c
     if x > sz:
         return False
     if x > r and x > c:
         return False
     if sz % x != 0:
         return False
     mlw = max_lw(x)
     if max(mlw) > max(r, c) or min(mlw) > min(r, c):
         return False
     #if min(r, c) == x / 2 and max(r, c) == x:
     #    print "!!"
     #    return False
     return True
 
 if __name__ == "__main__":
     fi = open('test.in', 'r')
     fo = open('test.out', 'w')
 
     size = int(fi.readline())
     trials = []
 
     for line in fi:
         # X, R, C
         trials.append([int(n) for n in line.split()])
 
     for T, trial in enumerate(trials):
         x = trial[0]
         r = trial[1]
         c = trial[2]
 
         winner = "GABRIEL" if can_do(x, r, c) else "RICHARD"
 
         towrite = "Case #{0}: {1}".format(T+1, winner)
         print(towrite, trial)
         fo.write(towrite + "\n")
 
     #print trials
 
     fi.close()
     fo.close()
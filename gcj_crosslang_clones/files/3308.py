import sys
 from time import time as ti
 
 def lastword(ini,out):
     f = open(ini,'r')
     o = open(out,'w')
     T = int(f.readline())
 
     for t in range(T):
         text = list(f.readline().rstrip())
         out = []
         out.append(text[0])
         for letter in text[1:]:
             if ord(letter) >= ord(out[0]):
                 out = list(letter) + out
             else:
                 out.append(letter)
         o.write("Case #"+str(t+1)+": "+"".join(out)+"\n")
         print("Case #"+str(t+1)+": "+"".join(out)+"\n")
 
 def main(argv):
     ini = "A-large.in"
     out = "A-large.txt"
     start = ti()
     lastword(ini,out)
     end = ti()
     print (end-start)
 
 if (__name__ == "__main__"):
     main(sys.argv[1:])

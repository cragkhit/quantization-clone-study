#!/c/Python27/python
 
 import sys
 
 def play_best(s):
 	last_word = [s[0]]
 	for c in s[1:]:
 		if(c >= last_word[0]):
 			last_word.insert(0,c)
 		else:
 			last_word.append(c)
 	return ''.join(last_word)
 
 
 play_best("CAB")
 	
 def main():
 	t = int(sys.stdin.readline().strip())
 	for i in range(1,t+1):
 		s = sys.stdin.readline().strip()
 		print "Case #%d: %s" % (i, play_best(s))
 
 if __name__ == '__main__':
 	main()
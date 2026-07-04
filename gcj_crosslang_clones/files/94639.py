def CountingSheep(n):
     digits = {'0':0,'1':0,'2':0,'3':0,'4':0,'5':0,'6':0,'7':0,'8':0,'9':0}
     total=0;
     k=1;
     if n==0:
         return "INSOMNIA";
     else:
         while (True):
             for i in str(n*k):
                 digits[i]=1;
                 #print "i = " +i;
             if not (0 in digits.values()):
                 return n*k;
             k+=1;
             #print k,n, " = ", digits, 0 in digits.values(), digits.values();
         
 
 ##if __name__ == "__main__":
 ##	testcases = input()
 ##	 
 ##	for caseNr in xrange(1, testcases+1):
 ##		cipher = raw_input()
 ##		print("Case #%i: %s" % (caseNr, solve(cipher)))
 
 in_file = open('C:\\Users\\mkader\\downloads\\A-large.in', 'r')
 ou_file = open('C:\\Users\\mkader\\downloads\\google_test_out.txt', 'w')
 
 case = 1;
 in_file.readline();
 for in_data in in_file:
     ldata = in_data.strip(' \t\n\r')
     if(len(ldata)==0):
         break;
     else:
         #print ldata,CountingSheep(int(ldata))  ;  
         print>>ou_file,'Case #'+str(case)+': '+str(CountingSheep(int(ldata)));
         case+=1;
 ou_file.close()
 ##print CountingSheep(0);
 ##print CountingSheep(1);
 ##print CountingSheep(2);
 ##print CountingSheep(11);
 ##print CountingSheep(1692);

#include <iostream>
 #include <stack>
 #include <cstdio>
 #include <math.h>
 #include <vector>
 #include <map>
 #include <set>
 #include <queue>
 #include <algorithm>
 #include <string>
 #include <iterator>
 using namespace std;
 
 #define A first
 #define B second
 
 long long powOfTen[18];
 vector<long long> fairAndSquareNumbers;
 
 bool isPalindrome(long long X)
 {
   int i, length;
   vector<int> temp;
   do
   {
     temp.push_back(X % 10LL);
     X/=10LL;
   }
   while (X > 0);
   length = temp.size();
   for (i=length/2; i>=0; --i)
   {
     if (temp[i] != temp[length-i-1])
       return false;
   }
   return true;
 }
 
 void testPalindromes(int length, long long multiplier, long long additional)
 {
   int i;
   long long num;
   if (length == 0)
   {
     num = additional;
     if (isPalindrome(num*num))
       fairAndSquareNumbers.push_back(num*num);
   }
   else if (length == 1)
   {
     for (i=0; i<10; ++i)
     {
       num = i*multiplier + additional;
       if (isPalindrome(num*num))
         fairAndSquareNumbers.push_back(num*num);
     }
   }
   else
   {
     if (additional > 0)
       i = 0;
     else
       i = 1;
     for (; i<10; ++i)
       testPalindromes(length-2, multiplier * 10, additional+(powOfTen[length-1]+1LL)*multiplier*i);
   }
 }
 
 int main()
 {
   long long i, j, T, result, N, num1, num2;
   
   powOfTen[0] = 1;
   for (i=1; i<18; ++i)
     powOfTen[i] = powOfTen[i-1]*10ll;
   for (i=1; i<10; ++i)
     testPalindromes(i, 1, 0);
   N = fairAndSquareNumbers.size();
   
   cin >> T;
   for (int t=1; t<=T; ++t)
   {
     cin >> num1;
     cin >> num2;
     result = 0;
     
     for (i=0; i<N; ++i)
       if (fairAndSquareNumbers[i] >= num1 && fairAndSquareNumbers[i] <= num2)
         ++result;
     
     cout << "Case #" << t << ": " << result << endl;
   }
 	return 0;
 }

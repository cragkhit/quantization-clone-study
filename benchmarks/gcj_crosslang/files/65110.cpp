#include <iostream>
 #include <string>
 #include <fstream>
 
 using namespace std;
 ifstream f ("ovat.in");
 ofstream g ("ovat.out");
 
 int t,s,v,ct;
 string sir;
 
 int main()
 {
     f>>t;
     for (int k=0; k<t; k++)
     {
         ct=0;
         f>>s;
         f>>sir;
         g<<"Case #"<<k+1<<": ";
         v= int(sir[0])-'0';
 
         for (int i=1; i<=s; i++)
         {
             if (i>v)
             {
                 ct++;
                 v+=i-v;
                 v+=int(sir[i])-'0';
             }
             else
                 v+=int(sir[i])-'0';
         }
         cout<<" "<<v<<" ";
         g<<ct<<'\n';
     }
     return 0;
 }

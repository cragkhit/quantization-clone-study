#include <iostream>
 #include <stdio.h>
 #include <math.h>
 #include <vector>
 #include <bitset>
 #define mod 1000000009
 using namespace std;
 
 string words[100];
 
 class type
 {
     public:
     char ch;
     int num;
 
     type(char c, int i)
     {
         ch = c;
         num = i;
     }
 };
 
 
 int abs(int a)
 {
     if(a<0)
         return -a;
     else
         return a;
 }
 int main()
 {
     int t, i, j, k, n, count;
     cin>>t;
     for(i=1; i<=t; i++)
     {
         cin>>n;
         vector <type> characters[n];
         for(j=0; j<n; j++)
             cin>>words[j];
 
         int ctr = 0;
         for(j=0; j<n; j++)
         {
             //cout<<words[j];
             int len = words[j].length();
             char cur = '*';
             count = 0;
             for(k=0; k<len; k++)
             {
                 if(cur==words[j][k])
                 {
                     count++;
                 }
                 else
                 {
                     if(cur!='*')
                     {
                         //cout<<j<<" "<<cur<<" "<<count<<endl;
                         characters[j].push_back(type(cur, count));
                         count = 1;
                         cur = words[j][k];
                     }
                     else
                     {
                         count = 1;
                         cur = words[j][k];
                     }
                 }
             }
             //cout<<j<<" "<<cur<<" "<<count<<endl;
             characters[j].push_back(type(cur, count));
         }
 
         int len = characters[0].size();
         for(j=1; j<n; j++)
         {
             if(len!=characters[j].size())
             {
               //  cout<<"here1"<<endl;
                 ctr = 1;
                 break;
             }
         }
         if(ctr==0)
         {
             for(j=0; j<len; j++)
             {
                 char cur = characters[0][j].ch;
                 for(k=1; k<n; k++)
                 {
                     if(characters[k][j].ch!=cur)
                     {
                        // cout<<"here2"<<endl;
                         ctr = 1;
                         break;
                     }
                 }
                 if(ctr==1)
                     break;
             }
         }
         if(ctr==1)
         {
             cout<<"Case #"<<i<<": "<<"Fegla Won\n";
         }
         else
         {
             count = 0;
             double cur_count = 0;
             int cur_cut;
             for(j=0; j<len; j++)
             {
                 cur_count = 0;
                 for(k=0; k<n; k++)
                     cur_count += characters[k][j].num;
 
                 //cout<<cur_count<<" ";
 
                 double avg = ((double)cur_count)/n;
                 //cout<<avg<<" ";
                 if(avg-(int)avg<0.5)
                     cur_cut = avg;
                 else
                     cur_cut = avg+1;
 
                 //cout<<cur_cut<<" ";
                 for(k=0; k<n; k++)
                     count += abs(characters[k][j].num-cur_cut);
 
                 //cout<<count<<endl;
             }
             cout<<"Case #"<<i<<": "<<count<<"\n";
         }
     }
     return 0;
 }

#include <iostream>
 #include <fstream>
 
 using namespace std;
 
 int main()
 {
     ifstream fin;
     fin.open("in.txt",ios::in);
     ofstream fout;
     fout.open("out.txt",ios::out|ios::trunc);
     int t;
     fin>>t;
     for(int i=0;i<t;i++)
     {
         int n;
         int m;
         fin>>n>>m;
         int lawn[n][m];
         for(int p=0;p<n;p++)
         {
             for(int q=0;q<m;q++)
             {
                 fin>>lawn[p][q];
             }
         }
         bool possible=true;
         for(int p=0;p<n;p++)
         {
             for(int q=0;q<m;q++)
             {
                 int val=lawn[p][q];
                 if(val<0||val>100)
                 {
                     possible=false;
                 }
                 int max1=0;
                 int max2=0;
                 int temp;
                 for(int u=0;u<n;u++)
                 {
                     temp=lawn[u][q];
                     if(temp>max1)
                     {
                         max1=temp;
                     }
                 }
                 for(int v=0;v<m;v++)
                 {
                     temp=lawn[p][v];
                     if(temp>max2)
                     {
                         max2=temp;
                     }
                 }
                 if((max1!=val)&&(max2!=val))
                 {
                     possible=false;
                 }
             }
         }
         fout<<"Case #"<<(i+1)<<": ";
         if(possible)
         {
             fout<<"YES";
         }
         else
         {
             fout<<"NO";
         }
         fout<<endl;
     }
     return 0;
 }

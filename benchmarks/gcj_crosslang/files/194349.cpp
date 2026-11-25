#include<iostream>
 #include<string>
 using namespace std;
 
 long long index(char c){
     return c-'A';
 }
 
 int main(){
     cin.tie(NULL);
     ios::sync_with_stdio(false);
     long long tests,t,d,i,j,letters[32],numbers[32];
     string vhod;
     string bukvi[16]={"ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE"};
     cin>>tests;
     for(t=1;t<=tests;t++){
         vhod.clear();
         for(i=0;i<30;i++){
             letters[i]=0;
             numbers[i]=0;
         }
         cin>>vhod;
         d=vhod.size();
         for(i=0;i<d;i++){
             letters[index(vhod[i])]++;
         }
         ///NULL
         numbers[0]=letters[index('Z')];
         for(i=0;i<bukvi[0].size();i++){
             letters[index(bukvi[0][i])]-=numbers[0];
         }
         ///TWO
         numbers[2]=letters[index('W')];
         for(i=0;i<bukvi[2].size();i++){
             letters[index(bukvi[2][i])]-=numbers[2];
         }
         ///FOUR
         numbers[4]=letters[index('U')];
         for(i=0;i<bukvi[4].size();i++){
             letters[index(bukvi[4][i])]-=numbers[4];
         }
         ///FIVE
         numbers[5]=letters[index('F')];
         for(i=0;i<bukvi[5].size();i++){
             letters[index(bukvi[5][i])]-=numbers[5];
         }
         ///SIX
         numbers[6]=letters[index('X')];
         for(i=0;i<bukvi[6].size();i++){
             letters[index(bukvi[6][i])]-=numbers[6];
         }
         ///EIGHT
         numbers[8]=letters[index('G')];
         for(i=0;i<bukvi[8].size();i++){
             letters[index(bukvi[8][i])]-=numbers[8];
         }
         ///SEVEN
         numbers[7]=letters[index('V')];
         for(i=0;i<bukvi[7].size();i++){
             letters[index(bukvi[7][i])]-=numbers[7];
         }
         ///THREE
         numbers[3]=letters[index('H')];
         for(i=0;i<bukvi[3].size();i++){
             letters[index(bukvi[3][i])]-=numbers[3];
         }
         ///ONE
         numbers[1]=letters[index('O')];
         for(i=0;i<bukvi[1].size();i++){
             letters[index(bukvi[1][i])]-=numbers[1];
         }
         ///NINE
         numbers[9]=letters[index('I')];
         for(i=0;i<bukvi[9].size();i++){
             letters[index(bukvi[9][i])]-=numbers[9];
         }
         cout<<"Case #"<<t<<": ";
         for(i=0;i<10;i++){
             for(j=0;j<numbers[i];j++){
                 cout<<i;
             }
         }
         cout<<"\n";
     }
     cout<<"\n";
     return 0;
 }

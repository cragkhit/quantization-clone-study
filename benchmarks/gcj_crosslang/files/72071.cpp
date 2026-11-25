#include <iostream>
 #include <vector>
 #include <string>
 #include <cmath>
 #include <algorithm>
 #include <cstdio>
 #include <cstdlib>
 
 using namespace std;
 
 string arr[4];
 
 int main(){
 	int T;
 	cin >> T;
 	for(int t=1;t<=T;t++){
 		for(int i=0;i<4;i++){
 			arr[i] = "";
 		}
 		for(int i=0;i<4;i++){
 			cin >> arr[i];
 		}
 
 		//cout << "map;\n";
 		//for(int i=0;i<4;i++){
 		//	cout << arr[i] << endl;
 		//}
 
 		int X, O, TT;
 		int judge = 0;
 		
 		// row
 		for(int i=0;i<4;i++){
 			X = O = TT = 0;
 			for(int j=0;j<4;j++){
 				if(arr[i][j]=='X')	X++;
 				if(arr[i][j]=='O')	O++;
 				if(arr[i][j]=='T')	TT++;
 			}
 			if(X+TT==4)	judge = 1;
 			else if(O+TT==4)	judge = 2;
 			if(judge)	break;
 		}
 
 		//	column
 		if(!judge){
 			for(int i=0;i<4;i++){
 				X = O = TT = 0;
 				for(int j=0;j<4;j++){
 					if(arr[j][i]=='X')	X++;
 					if(arr[j][i]=='O')	O++;
 					if(arr[j][i]=='T')	TT++;
 				}
 				if(X+TT==4)	judge = 1;
 				else if(O+TT==4)	judge = 2;
 				if(judge)	break;
 			}
 		}
 		//else{
 		//	cout << "row hit!\n";
 		//}
 
 		//	naname
 		if(!judge){
 			X = O = TT = 0;
 			for(int i=0;i<4;i++){
 				if(arr[i][i]=='X')	X++;
 				if(arr[i][i]=='O')	O++;
 				if(arr[i][i]=='T')	TT++;
 			}
 			if(X+TT==4)	judge = 1;
 			else if(O+TT==4)	judge = 2;
 		}
 		if(!judge){
 			X = O = TT = 0;
 			for(int i=0;i<4;i++){
 				if(arr[i][3-i]=='X')	X++;
 				if(arr[i][3-i]=='O')	O++;
 				if(arr[i][3-i]=='T')	TT++;
 			}
 			if(X+TT==4)	judge = 1;
 			else if(O+TT==4)	judge = 2;
 		}
 		//else{
 		//	cout << "column hit!\n";
 		//}
 
 		// result
 		if(!judge){
 			X = O = TT = 0;
 			for(int i=0;i<4;i++){
 				for(int j=0;j<4;j++){
 					if(arr[i][j]=='X')	X++;
 					if(arr[i][j]=='O')	O++;
 					if(arr[i][j]=='T')	TT++;				
 				}
 			}
 			if(X+O+TT==16){
 				cout << "Case #" << t << ": Draw" << endl;
 			}else{
 				cout << "Case #" << t << ": Game has not completed" << endl;
 			}
 		}
 		else{
 			if(judge==1){
 				cout << "Case #" << t << ": X won" << endl;
 			}else{
 				cout << "Case #" << t << ": O won" << endl;
 			}
 		}
 	}
 }
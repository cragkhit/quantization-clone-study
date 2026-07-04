#include<iostream>
 #include<fstream>
 #include<string>
 using namespace std;
 int main()
 {
 	ifstream file1;
 	file1.open("D-large.IN");
 	ofstream file2;
 	file2.open("outfinal.OUT");
 	//-------------------------------
 	string tcases;
 	getline(file1, tcases);
 	int t = stoi(tcases);
 	string var;
 	string ga = "GABRIEL";
 	string ri = "RICHARD";
 	for (int i = 1; i <= t; i++)
 	{
 		getline(file1, var);
 		int f = var.find(' ');
 		int l = var.rfind(' ');
 		string xs = var.substr(0, f);
 		string rs = var.substr(f + 1, l-f);
 		string cs = var.substr(l + 1);
 		int x = stoi(xs);
 		int r = stoi(rs);
 		int c = stoi(cs);
 		float min,max;
 		float x2 = x*1.0;
 		if ((r*c) % x == 0)
 		{
 			if (r <= c)
 			{
 				min = r*1.0; max = c*1.0;
 			}
 			else
 			{
 				min = c*1.0;
 				max = r*1.0;
 			}
 			if ((x == 2) && (min == 1) && ((int)max % 2 == 0))
 			{
 				file2 << "Case #" << i << ": " << ga << endl;
 				continue;
 			}
 			if (min > (x2 / 2.0) && max >= x2)
 				file2 << "Case #" << i << ": " << ga << endl;
 			else file2 << "Case #" << i << ": " << ri <<  endl;
 		}
 		else file2 << "Case #" << i << ": " << ri <<  endl;
 		
 	}
 	
 }
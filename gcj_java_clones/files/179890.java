import java.util.*;
 class codeJam{
 	public static void main(String args[]){
 		Scanner s = new Scanner(System.in);
 		int t = s.nextInt();int count = 1;
 		while(t>0){
 			String st = s.next();
 			String f = "";
 			while(st.indexOf('Z')!=-1 && st.indexOf('E')!=-1 && st.indexOf('R')!=-1 && st.indexOf('O')!=-1 ){
 				f += "0";
 				st = st.replaceFirst("Z","");
 				st = st.replaceFirst("E","");
 				st = st.replaceFirst("R","");
 				st = st.replaceFirst("O","");
 			}
 			while(st.indexOf('T')!=-1 && st.indexOf('W')!=-1 && st.indexOf('O')!=-1){
 				f += "2";
 				st = st.replaceFirst("T","");
 				st = st.replaceFirst("W","");
 				st = st.replaceFirst("O","");
 			}
 			while(st.indexOf('F')!=-1 && st.indexOf('O')!=-1 && st.indexOf('U')!=-1 && st.indexOf('R')!=-1){
 				f += "4";
 				st = st.replaceFirst("F","");
 				st = st.replaceFirst("U","");
 				st = st.replaceFirst("R","");
 				st = st.replaceFirst("O","");
 			}
 			while(st.indexOf('S')!=-1 && st.indexOf('I')!=-1 && st.indexOf('X')!=-1){
 				f += "6";
 				st = st.replaceFirst("S","");
 				st = st.replaceFirst("I","");
 				st = st.replaceFirst("X","");
 			}
 			while(st.indexOf('E')!=-1 && st.indexOf('I')!=-1 && st.indexOf('G')!=-1 && st.indexOf('H')!=-1 && st.indexOf('T')!=-1 ){
 				f += "8";
 				st = st.replaceFirst("E","");
 				st = st.replaceFirst("I","");
 				st = st.replaceFirst("G","");
 				st = st.replaceFirst("H","");
 				st = st.replaceFirst("T","");
 			}
 			while(st.indexOf('O')!=-1 && st.indexOf('N')!=-1 && st.indexOf('E')!=-1){
 				st = st.replaceFirst("O","");
 				st = st.replaceFirst("N","");
 				st = st.replaceFirst("E","");
 				f += "1";
 			}
 			while(st.indexOf('F')!=-1 && st.indexOf('I')!=-1 && st.indexOf('V')!=-1 && st.indexOf('E')!=-1){
 				f += "5";
 				st = st.replaceFirst("F","");
 				st = st.replaceFirst("I","");
 				st = st.replaceFirst("V","");
 				st = st.replaceFirst("E","");
 			}
 
 			while(st.indexOf('T')!=-1 && st.indexOf('H')!=-1 && st.indexOf('E')!=-1 && st.indexOf('R')!=-1){
 				st = st.replaceFirst("E","");
 				if(st.indexOf("E")!=-1){
 					st = st.replaceFirst("T","");
 					st = st.replaceFirst("H","");
 					st = st.replaceFirst("E","");
 					st = st.replaceFirst("R","");
 					f += "3";
 				}
 				else{st+="E";break;}
 			}
 			
 			
 			while(st.indexOf('S')!=-1 && st.indexOf('E')!=-1 && st.indexOf('V')!=-1 && st.indexOf('N')!=-1){
 				st = st.replaceFirst("E","");
 				if(st.indexOf("E")!=-1){
 					st = st.replaceFirst("S","");
 				st = st.replaceFirst("V","");
 				st = st.replaceFirst("E","");
 				st = st.replaceFirst("N","");
 				f += "7";
 				}
 				else{st+="E";break;}
 			}
 			while(st.indexOf('N')!=-1 && st.indexOf('I')!=-1 && st.indexOf('E')!=-1 ){
 				st = st.replaceFirst("N","");
 				if(st.indexOf("N")!=-1){
 				st = st.replaceFirst("N","");
 				st = st.replaceFirst("I","");
 				st = st.replaceFirst("E","");
 				f += "9";
 				}
 				else{st+="N";break;}
 			}
 
 			char[] chars = f.toCharArray();
         Arrays.sort(chars);
         f = new String(chars);
  
 			System.out.println("Case #"+count+": "+f);
 			count++;
 			t--;
 		}
 	}
 }

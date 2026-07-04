import java.util.*;
 import java.math.*;
 class solution{
 	public static void main(String args[]){
 		Scanner sc = new Scanner(System.in);
 		int n = sc.nextInt();
 		for(int i=0;i<n;i++){
 			int N = sc.nextInt();
 			String start = new String();
 			strings arr[] = new strings[N];
 			int flag=0;
 			for(int j=0;j<N;j++){
 				String str =sc.next();
 				if(j==0){
 					start = removeDup(str);
 				}
 				else{
 					if(!removeDup(str).equals(start)){flag=1;}
 				}
 //				System.out.println(str);
 				strings temp =new strings(start.length(),str,start);
 				arr[j]=temp;
 			}
 			if(flag==1){System.out.println("Case #"+(i+1)+": Fegla Won");continue;}
 
 			int max[] = new int[start.length()];
 			int min[] = new int[start.length()];
 //			System.out.println("chk "+arr[0].count[2]);
 			for(int j=0;j<start.length();j++){
 				int mini = 101;
 				int maxi = 0;
 				for(int k=0;k<N;k++){
 					if(arr[k].count[j]<mini){mini=arr[k].count[j];}
 					if(arr[k].count[j]>maxi){maxi=arr[k].count[j];}
 				}
 				max[j]=maxi;
 				min[j]=mini;
 //				System.out.println("Min = "+min[j]+" max[j] "+max[j]);
 			}
 			int total=0;
 			for(int j=0;j<start.length();j++){
 				int minOp = 1000,knt=0;
 				for(int k = min[j];k<=max[j];k++){
 					int op = 0;
 					for(int l = 0;l<N;l++){
 						int n2 = arr[l].count[j];
 						if(n2>k){op=op+n2-k;}
 						else{op=op+k-n2;}
 					}
 					if(minOp>op){minOp=op;knt=k;}
 				}
 //				System.out.println("index = "+j+" - Count "+knt);
 				total=total+minOp;
 			}
 			System.out.println("Case #"+(i+1)+": "+total);
 		}
 	}
 	public static String removeDup(String s){
 		StringBuffer str = new StringBuffer("");
 		char prev=s.charAt(0);
 		str.append(prev);
 		for(int i=1;i<s.length();i++){
 			char c = s.charAt(i);
 			if(prev==c){continue;}
 			prev=c;
 			str.append(c);
 		}
 		return str.toString();
 	}
 }
 class strings{
 	int count[];
 	strings(int length,String s,String start){
 				this.count = new int[length];
 //				char c = s.charAt(0);
 				int cnt=0,index=0,pointer=0;
 				while(index<start.length()){
 					char c = start.charAt(index);
 					cnt=0;
 					while(pointer<s.length() && s.charAt(pointer)==c){pointer++;cnt++;}
 					count[index]=cnt;
 					index++;
 				}
 		}
 }
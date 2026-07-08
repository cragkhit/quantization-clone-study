import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 
 public class Main {
 
 	static Main main;
 
 	public static void main(String[] args) {
 		long [] pow2 = new long[50];
 		pow2[0] = 1;
 		for(int i = 1 ; i < 50 ; i++) {
 			pow2[i] = pow2[i-1] * 2;
 		}
 		main = new Main();
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		BufferedOutputStream bos = new BufferedOutputStream(System.out);
 		String eol = System.getProperty("line.separator");
 		byte[] eolb = eol.getBytes();
 		byte[] spaceb = " ".getBytes();
 		byte[] caseb = "Case #".getBytes();
 		byte[] colonb = ":".getBytes();
 		try {
 			String str = br.readLine();
 			int t = Integer.parseInt(str);
 			for(int i = 0 ; i < t ; i++) {
 
 				str = br.readLine();
 				int blank = str.indexOf("/");
 				long p = Long.parseLong(str.substring(0,blank));
 				long q = Long.parseLong(str.substring(blank+1));
 				if(p!=0) {
 					long g = gcd(p,q);
 					p/=g;
 					q /= g;
 				}
 				int index = Arrays.binarySearch(pow2, q);
 				bos.write(caseb);
 				bos.write(new Integer(i+1).toString().getBytes());
 				bos.write(colonb);
 				bos.write(spaceb);
 				if(index<0) {
 					bos.write("impossible".getBytes());
 				} else {
 					int ans = 0;
 					for(int j = 1 ; j < 50 ; j++) {
 						// if p/q >= 1/2^j then j is possible => p * 2^j >= q
 						if(p*pow2[j]>=q) {
 							ans = j;
 							break;
 						}
 					}
 					bos.write(new Integer(ans).toString().getBytes());
 				}
 				bos.write(eolb);
 			}
 			bos.flush();
 		} catch(IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	public static long gcd(long a , long b) {
 		if(a<b) {
 			long temp = a;
 			a = b;
 			b = temp;
 		}
 		if((a%b)==0) {
 			return b;
 		}
 		return gcd(b,a%b);
 	}
 
 }

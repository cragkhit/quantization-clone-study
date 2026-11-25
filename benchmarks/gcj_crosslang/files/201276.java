import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 class Problem1 {
     public static void main(String[] args)
         throws Exception {
         new Problem1().start(args[0]);
     }
 
     public void start(String fileName)
         throws Exception {
         InputStreamReader is =
             new InputStreamReader(
                                   new FileInputStream(fileName));
         BufferedReader br = new BufferedReader(is);
         OutputStreamWriter os =
             new OutputStreamWriter(
                                    new FileOutputStream("Output"));
         BufferedWriter bw = new BufferedWriter(os);
         String line;
 
         int test = 1;
         line = br.readLine();
         int tot_test = Integer.parseInt(line);
         while (tot_test >= test) {
             System.out.println("Case #" + test++ + ": " + calc(br));
         }
     }
 
     String[] nums = {"ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE"};
 
     String calc(BufferedReader br) throws Exception {
         String s = br.readLine();
         String result = "";
         int[] freq = new int[10];
         char[] arr = s.toCharArray();
         /*for (int i = 0; i <= 9; i++) {
             int cnt = count(s, nums[i]);
             while (cnt > 0) {
                 result += Integer.toString(i);
                 cnt--;
             }
         }
         */
         int sanity = 0;
         while (done(arr) == false) {
             if (contains(arr, 'X') == true) {
                 replace(arr, 'S', 1);
                 replace(arr, 'I', 1);
                 replace(arr, 'X', 1);
                 freq[6]++;
             } else if (contains(arr, 'W') == true) {
                 String str = nums[2];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[2]++;
             } else if (contains(arr, 'Z') == true) {
                 String str = nums[0];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[0]++;
             }  else if (contains(arr, 'G') == true) {
                 String str = nums[8];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[8]++;
             }  else if (contains(arr, 'H') == true) {
                 String str = nums[3];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[3]++;
             } else if (contains(arr, 'S') == true) {
                 String str = nums[7];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[7]++;
             } else if (contains(arr, 'V') == true) {
                 String str = nums[5];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[5]++;
             } else if (contains(arr, 'F') == true) {
                 String str = nums[4];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[4]++;
             } else if (contains(arr, 'O') == true) {
                 String str = nums[1];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[1]++;
             } else {
                 String str = nums[9];
                 for (int i = 0; i <= str.length() - 1; i++)
                     replace(arr, str.charAt(i), 1);
                 freq[9]++;
             }
             if (sanity++ > 1000) break;
         }
         for (int i = 0; i <= freq.length - 1; i++) {
             while (freq[i]-- > 0)
                 result += Integer.toString(i);
         }
 
         return result;
     }
 
 
     boolean done(char[] arr) {
         for (char ch : arr) if (ch != '0') return false;
         return true;
     }
 
     boolean contains(char[] arr, char c) {
         for (char ch : arr) if (ch == c) return true;
         return false;
     }
 
     int count(String s, String num) {
         int[] count = new int[num.length()];
         for (int i = 0; i <= s.length() - 1; i++) {
             for (int j = 0; j <= num.length() - 1; j++) {
                 if (s.charAt(i) == num.charAt(j)) count[j]++;
             }
         }
         int result = count[0];
         for (int i = 0; i <= count.length - 1; i++) {
             //System.out.println(count[i]);
             if (count[i] == 0) return 0;
             result = Math.min(result, count[i]);
             for (int j = i + 1; j <= count.length - 1; j++) {
                 if (num.charAt(i) == num.charAt(j)) count[j]--;
             }
         }
         return result;
     }
 
     void replace(char[] arr, char c, int num) {
         //System.out.println("replacing " + c);
         int i = 0;
         while (num > 0) {
             if (arr[i] == c) {
                 arr[i] = '0';
                 num--;
             }
             i++;
         }
     }
 }

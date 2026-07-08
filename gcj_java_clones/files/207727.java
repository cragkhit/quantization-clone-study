package jam2016;
 
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.*;
 
 /**
  * https://code.google.com/codejam/contest/4314486/dashboard#s=p0
  */
 public class A_1C {
 
     public Scanner sc = new Scanner(getClass().getResourceAsStream(IN));
     public static final String FILENAME = "A_1C-large";
     public static final String IN = FILENAME + ".in";
     public static final String OUT = FILENAME + ".out";
     public PrintStream out = System.out;
     public boolean writeToFile = true;
 
     public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V>
     sortByValue( Map<K, V> map )
     {
         List<Map.Entry<K, V>> list =
                 new LinkedList<>( map.entrySet() );
         Collections.sort( list, new Comparator<Map.Entry<K, V>>()
         {
             @Override
             public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
             {
                 return ( o2.getValue() ).compareTo( o1.getValue() );
             }
         } );
 
         LinkedHashMap<K, V> result = new LinkedHashMap<>();
         for (Map.Entry<K, V> entry : list)
         {
             result.put( entry.getKey(), entry.getValue() );
         }
         return result;
     }
 
     private void solve() {
         int ans = 0;
         char chA = 'A';
         int pCount = sc.nextInt();
         int mCount=0;
         //int[] parties = new int[pCount];
         Map<Character,Integer>parties=new HashMap<>();
         for (int i = 0; i < pCount; i++) {
             final int count = sc.nextInt();
             parties.put((char) (chA + i), count);
             mCount+=count;
         }
         LinkedHashMap<Character,Integer> sortMap=null;
         do{
             if(mCount==0) {
                 break;
             }
             sortMap = sortByValue(parties);
             final Map.Entry<Character, Integer> first = sortMap.entrySet().iterator().next();
 
             parties.put(first.getKey(),first.getValue()-1);
             mCount--;
 
             if( mCount==2) {
                 out.print(" " + first.getKey());
                 continue;
             }
 
             sortMap = sortByValue(parties);
             final Map.Entry<Character, Integer> second = sortMap.entrySet().iterator().next();
 
             parties.put(second.getKey(),second.getValue()-1);
             mCount--;
             out.print(" " + first.getKey()+second.getKey());
         }while (mCount>0);
         out.println();
 
 
     }
 
     private void run() throws Exception {
         if (writeToFile) {
             out = new PrintStream(new FileOutputStream(OUT));
         }
         int t = sc.nextInt();
         for (int i = 1; i <= t; i++) {
             out.print("Case #" + i + ":");
             solve();
         }
         sc.close();
         out.close();
     }
 
     public static void main(String args[]) throws Exception {
         new A_1C().run();
     }
 
 }
<?php
 
 $input = file('C:\Users\Razel\Desktop\B-large.in');
 
 $caseLine = 1;
 for($case = 0; $case < $input[0]; $case++)
 {
     list($y, $x) = explode(' ', trim($input[$caseLine]));
     
     $lawn = array();
     for($cy = 0; $cy < $y; $cy++)
     {
         $lawn[$cy] = explode(' ', trim($input[$caseLine+$cy+1]));
         $ymax[$cy] = max($lawn[$cy]);
     }
     
     $impossible = false;
     
     for($cx = 0; $cx < $x; $cx++)
     {
         $xmax[$cx] = 0;
         for($cy = 0; $cy < $y; $cy++)
         {
             $xmax[$cx] = max($xmax[$cx], $lawn[$cy][$cx]);
         }
     }
     
     for($cy = 0; $cy < $y; $cy++)
     {
         for($cx = 0; $cx < $x; $cx++)
         {
             $impossible = $impossible || !in_array($lawn[$cy][$cx], array($xmax[$cx], $ymax[$cy]));
         }
     }
     
     echo "Case #".($case+1).": ".($impossible?'NO':'YES')."\n";
     
     $caseLine += $y+1;
 }

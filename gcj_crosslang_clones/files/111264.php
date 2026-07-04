<?php 
 
 $input = ''; $in = fopen('php://stdin', 'r'); while(!feof($in)){ $input .= fgets($in, 4096); }
 
 print mushroom_minimums($input);
 
 function mushroom_minimums($input) {
   $input = explode("\n", $input);
   array_shift($input);
   array_pop($input);
   $res = array();
   for ($k=1; $k<=count($input);$k+=2) {
     $plates = explode(' ', $input[$k]);
     $res[] = calculate_minimums($plates);
   }
   $out = array();
   foreach ($res as $k=>$r) {
 	  $out[] = 'Case #'.($k+1).': '.$r;
   }
   return implode("\n", $out);
 }
 
 function calculate_minimums($plates) {
   $minimum1 = 0;
   $minimum2 = 0;
   $largest = 0;
   for ($i=0; $i<count($plates)-1; $i+=1) {
     $diff = $plates[$i] - $plates[$i+1];
     if ($diff > 0) {
       $minimum1 += $diff;
       if ($diff > $largest) {
         $largest = $diff;
       }
     }
   }
 
   for ($i=0; $i<count($plates)-1; $i+=1) {
     $minimum2 += ($largest < $plates[$i]) ? $largest : $plates[$i];
   }
 
   return $minimum1 . ' ' . $minimum2;
 }

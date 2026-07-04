#!/usr/bin/php
 <?php
 
 $file = $argv[1];
 
 $fh = fopen($file, 'r');
 
 $num_cases = fgets($fh);
 $num_cases = substr($num_cases, 0, strlen($num_cases)-1);
 
 $output = array();
 
 for ($i=1; $i<=$num_cases; $i++) {
     $input = fgets($fh);
     $input = substr($input, 0, strlen($input)-1);    
 
     $data = split(' ', $input);
 
     //echo "$data[0] - $data[1]\n";
 
     $r = $data[0];
     $t = $data[1];
     
     //$a = 2 * $r + 1;
     $b = 2 * $r - 1;
 
     $n1 = (-$b + sqrt($b*$b+8*$t)) / 4;
     $n1_int = floor($n1);
 
     //if ($n1 != $n1_int) {
       $result = $n1_int;
     /* }
     else {
       // check again
       $num_circles = 0;
       $n = 1;
       $tpused = 0;
 
       while (true) {
       //$r1 = $r + 2 * ($n - 1);
       //$r2 = $r + (2 * $n - 1);
       //$pused = $r2 * $r2 - $r1 * $r1;
 
         $pused = (2 * $r) + (4 * $n) - 3;
 //echo "painted used $pused\n";
         $tpused += $pused;
         if ($tpused <= $t) {
       	  $num_circles++;
 	   $n++;
         }
         else {
       	  $result = $num_circles;
       	  break;
         }
       }
     } */
 
     //echo "Result: $result\n";
     // push the result into an array
     array_push($output, "Case #{$i}: " . $result);
 }
 
 foreach ($output as $output_elm) {
   echo "$output_elm\n";
 }
 
 fclose($fh);
 ?>

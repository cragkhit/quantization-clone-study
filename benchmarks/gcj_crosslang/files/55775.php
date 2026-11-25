<?php
 
 // Google Code Jam Qualification Round 2015
 // Problem B. Infinite House of Pancakes
 // Solution in PHP
 // By Smithers
 
 function reduce_stacks($a, $tgt) {
     $result = 0;
     foreach ($a as $p) {
         $result += (int)(($p - 1) / $tgt);
     }
     return $result;
 }
 
 function solve_case($a) {
     $max = max($a);
     $result = $max;
     
     for ($i = 1; $i < $max; $i++) {
         $j = reduce_stacks($a, $i);
         if ($i + $j < $result) {
             $result = $i + $j;
         }
     }
     
     return $result;
 }
 
 $T = (int)fgets(STDIN);
 
 for ($x = 1; $x <= $T; $x++) {
     $D = (int)fgets(STDIN);
     $a = array_map('intval', explode(' ', fgets(STDIN)));
     
     $res = solve_case($a);
     
     echo "Case #$x: $res\n";
 }

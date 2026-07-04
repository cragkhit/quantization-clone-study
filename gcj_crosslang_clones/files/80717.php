<?php
 $input_file = "D-large.in";
 $output_file = "D-large-answer.in";
 
 $f = fopen($input_file,'r');
 
 $t = (int) fgets($f);
 
 $answers = array();
 
 echo '<pre>';
 for($i=0;$i<$t;$i++) {
     $kcs = explode(' ',trim(fgets($f)));
 
     $k = (int) $kcs[0];
     $c = (int) $kcs[1];
     $s = (int) $kcs[2];
 
     if($c==1) {
        if($k == $s) {
             $gap = pow($k,$c-1);
             $checks = array();
             for($ch = 0; $ch<$s; $ch++) {
                 $checks[] = ($ch * $gap) + 1;
             }
             $answers[] = join(' ',$checks);
         } else {
            $answers[] = "IMPOSSIBLE";
        }
     } else {
         $gap = pow($k,$c-1);
         $total = pow($k,$c);
         $checks = array();
         $mid = ceil($k/2);
         for($j=1;$j<=$mid;$j+=2) {
             $ch = ($gap * $j) + $j;
             if($ch > $total) {
                 $ch -= $j;
             }
             $checks[] = $ch;
             $opposite = $total - ($ch - 1);
             if ($opposite > $ch) {
                 $checks[] = $opposite;
             }
         }
         if(count($checks) > $s) {
             $answers[] = "IMPOSSIBLE";
         } else {
             $answers[] = join(' ',$checks);
         }
     }
 }
 
 $f2 = fopen($output_file,'w');
 
 foreach($answers as $k => $a) {
     $line = 'Case #'.($k+1).": $a\n";
     fwrite($f2,$line);
     echo $line;
 }
 echo '</pre>';
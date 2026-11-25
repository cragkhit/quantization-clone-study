<?php
 
 set_time_limit(600);
 
 $inputFile  = 'input.txt';
 $outputFile = 'output.txt';
 $handleIn   = fopen($inputFile, "rb");
 $handleOut  = fopen($outputFile, "w+");
 $counter    = 0;
 $i          = 0;
 
 $squares = array();
 for($i=1;$i<=pow(10,14);$i++) {
     $square = $i*$i;
     if($square>pow(10,14)) break;
     if((int)strrev($square)==$square && (int)strrev($i)==$i) {
         $squares[] = $square;
     }
 }
 
 //print_r($squares);
 
 $i          = 0;
 
 while (($lineIn = fgets($handleIn, 4096)) !== false) {
     
     if($lineIn=='') continue;
 
     if($counter==0) {
         $counter = (int) $lineIn;
         continue;
     }
     
     if($i==$counter) break;
 
     $lineOut = FairAndSquare($lineIn, $squares);
 
     $i++;
     
     echo $echo = 'Case #'.$i.': ' . $lineOut."\n";
     fwrite($handleOut, $echo);
 }
 
 fclose($handleIn);
 fclose($handleOut);
 
 
 function FairAndSquare($line, $squares) {
 
     $interval = explode(' ', trim($line));
     $total = 0;
 
     foreach($squares as $square) {
         if($square>=$interval[0] && $square<=$interval[1]) {
             $total++;
         }
     }
 
     return $total;
 }
 
 ?>
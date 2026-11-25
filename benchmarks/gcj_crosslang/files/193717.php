<?php
 function proc(&$cnt, $c, $s) {
   $res = $cnt[ord($c)];
   $len = strlen($s);
   for ($i = 0; $i < $len; $i++) {
     $x = ord($s[$i]);
     $cnt[$x] -= $res;
   }
   return $res;
 }
 fscanf(STDIN, "%D", $t);
 for ($ti = 1; $ti <= $t; $ti++) {
   fscanf(STDIN, "%s", $str);
   $cnt = array();
   for ($i = 65; $i <= 90; $i++)
     $cnt[$i] = 0;
   $len = strlen($str);
   for ($i = 0; $i < $len; $i++) {
     $x = ord($str[$i]);
     $cnt[$x] += 1;
   }
   $list = array();
   $list[0] = proc($cnt, "Z", "ZERO");
   $list[6] = proc($cnt, "X", "SIX");
   $list[4] = proc($cnt, "U", "FOUR");
   $list[2] = proc($cnt, "W", "TWO");
   $list[5] = proc($cnt, "F", "FIVE");
   $list[7] = proc($cnt, "V", "SEVEN");
   $list[1] = proc($cnt, "O", "ONE");
   $list[8] = proc($cnt, "G", "EIGHT");
   $list[3] = proc($cnt, "T", "THREE");
   $list[9] = proc($cnt, "E", "NINE");
   echo "Case #" . $ti . ": ";
   for ($i = 0; $i <= 9; $i++) {
     for ($j = 0; $j < $list[$i]; $j++)
       echo $i;
   }
   echo "\n";
 }
 ?>

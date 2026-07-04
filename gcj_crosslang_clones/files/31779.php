<?php
 
 $fin = fopen("a.in", "r");
 $firstLine = fgets($fin);
 sscanf($firstLine, "%d", $cases);
 
 function calc($r, $num) {
 	return $num * (2 * $r + 2 * $num -1);
 }
 for($i = 1; $i <= $cases; $i++){
 	$line = fgets($fin);
         sscanf($line, "%d %d", $r, $t);
         $left = 1;
 	$right = 100000000000000000000;
     	while($left < $right - 1) {
     		$middle = floor(($left + $right) / 2);
 		if (calc($r, $middle) > $t) $right = $middle;
 		else $left = $middle;
 	}
 	if (calc($r, $right) <= $t) $result = floor($right);
 	else $result = floor($left);
 	echo "Case #$i: $result\n";
     }
 

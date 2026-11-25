<?php
 
 chdir(dirname(__FILE__));
 
 //$f = fopen('./exemple.txt', 'r');
 $f = fopen('./B-large.in', 'r');
 
 fscanf($f, '%d', $nbTests);
 for ($testNum=1; $testNum<=$nbTests; $testNum++) {
 	echo 'Case #'.$testNum.': ';
 	
 	fscanf($f, '%d', $n);
 	$array = explode(' ', trim(fgets($f)));
 	
 	$max = max($array);
 	$best = $max;
 	for ($i=1; $i<$max; $i++) {
 		$result = test($i, $array);
 		if ($result < $best) {
 			$best = $result;
 		}
 	}
 	echo $best;
 	
 	echo "\n";
 }
 
 fclose($f);
 
 function test($max, $array) {
 	$result = $max;
 	foreach ($array as $v) {
 		if ($v > $max) {
 			$result += ceil($v/$max) - 1;
 		}
 	}
 	//echo "test($max) = $result\n";
 	return $result;
 }

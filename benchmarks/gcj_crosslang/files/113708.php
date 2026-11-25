<?php
 
 function bleatrix($base){
 	if ($base === 0) return "INSOMNIA";
 	$missing = ["0","1","2","3","4","5","6","7","8","9"];
 	$i = 1;
 	while (true){
 		$value = $i++ * $base;
 		$missing = array_diff($missing,str_split(strval($value)));
 		if (count($missing) === 0) return $value;
 	}
 }
 
 $count = intval(fgets(STDIN));
 for ($i = 1; $i <= $count; $i++){
 	$sleep = bleatrix(intval(fgets(STDIN)));
 	echo "Case #$i: $sleep\n";
 }
 
 

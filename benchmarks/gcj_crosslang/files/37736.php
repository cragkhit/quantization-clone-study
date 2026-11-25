<?php
 $file = 'D-large.in';
 $lines = explode("\n", file_get_contents($file));
 
 
 $T = $lines[0];
 
 for($t=0; $t<$T; $t++) {
 	$line_per_case = 1;
 	$data = explode(' ', $lines[$t*$line_per_case+1]);
 	$K = $data[0];
 	$C = $data[1];
 	$S = $data[2];
 
 	$Smin = ceil($K/$C);
 	if($S < $Smin) {
 		echo "Case #".($t+1).": IMPOSSIBLE\n";
 		continue;
 	}
 	if($K == 1) {
 		echo "Case #".($t+1).": 1\n";
 		continue;
 	}
 
 	$result = [];
 	$position = 0;
 	for($k=0; $k<$K; $k++) {
 		$pow = $C-1-($k%$C);
 		$position += $k * pow($K, $pow);
 
 		if($pow == 0) {
 			$result[] = $position + 1;
 			$position = 0;
 		}
 	}
 
 	if($position > 0) $result[] = $position + 1;
 
 	echo "Case #".($t+1).": ".implode(' ', $result)."\n";
 }
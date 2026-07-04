<?php
 
 echo "\n";
 ob_start();
 
 $in = fopen('php://stdin', 'r');
 $lines = [];
 while(!feof($in)) {
 	$line = fgets($in);
 	if(trim($line))
 		$lines[] = explode('/', trim($line));
 }
 fclose($in);
 array_shift($lines);
 
 $anc = gmp_pow(2, 40);
 $log2 = log(2);
 foreach($lines as $key => $line) {
 	list($p, $q) = $line;
 	
 	$ratio = $q / $p;
 	if(floor($ratio) == $ratio) {
 		$p /= $q / $ratio;
 		$q = $ratio;
 	}
 	
 	$result = gmp_strval(gmp_div_r($anc, $q)) == 0 ? ceil(abs(log($p / $q) / $log2)) : 'impossible';
 	
 	echo 'Case #', $key + 1, ': ', $result, "\n";
 }
 
 file_put_contents('out.txt', trim(ob_get_flush()));
 echo "\n";
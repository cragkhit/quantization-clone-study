<?php
 $filePointer = fopen($argv[1], "r");
 
 $caseCount = trim(fgets($filePointer));
 for ($i = 1; $i <= $caseCount; $i++) {
 	echo "Case #" . $i . ": ";
 	
 	$timeInterval = trim(fgets($filePointer));
 	$countAtTime = explode(" ", trim(fgets($filePointer)));
 	
 	$firstMethod = 0;
 	$maxDifferent = 0;
 	for ($j = 1; $j < $timeInterval; $j++) {
 		$countDifferent = $countAtTime[$j - 1] - $countAtTime[$j];
 		if ($countDifferent > 0) $firstMethod += $countDifferent;
 		if ($countDifferent > $maxDifferent) $maxDifferent = $countDifferent;
 	}
 	$secondMethod = 0;
 	for ($j = 0; $j < $timeInterval - 1; $j++) {
 		$secondMethod += min($countAtTime[$j], $maxDifferent);
 	}
 	
 	echo $firstMethod . " " . $secondMethod . "\n";
 }
 
 fclose($filePointer);
 ?>
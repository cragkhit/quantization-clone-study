<?php
 
 $fh = STDIN;
 $countTestCases = intval(fgets($fh));
 
 function printWin($i, $x) {
 	echo sprintf("Case #%d: %d %d\n", $i, $x[0], $x[1]);
 }
 
 function calculateX(&$listCountMushrooms, $i, $l, $accX, $accY) {
 	if ($i === $l - 1) {
 		return [$accX, $accY];
 	}
 
 	$eaten = $listCountMushrooms[$i] - $listCountMushrooms[$i + 1];
 
 	if ($eaten > 0) {
 		$accX += $eaten;
 	}
 
 	if ($eaten > $accY) {
 		$accY = $eaten;
 	}
 
 	return calculateX($listCountMushrooms, $i + 1, $l, $accX, $accY);
 }
 
 for ($indexCase = 0; $indexCase < $countTestCases; $indexCase += 1) {
 	$countRefills = intval(fgets($fh));
 	$listCountMushrooms = array_map('intval', preg_split('/\s/', trim(fgets($fh), "\n\r")));
 
 	$x = calculateX($listCountMushrooms, 0, $countRefills, 0, 0);
 
 	$eatenY = 0;
 	for ($i = 0; $i < $countRefills - 1; $i += 1) {
 		$eatenY += min([$listCountMushrooms[$i], $x[1]]);
 	}
 
 	$x[1] = $eatenY;
 
 	printWin($indexCase + 1, $x);
 }

<?php
 define('NA', -1);
 define('RICHARD', 0);
 define('GABRIEL', 1);
 
 $fh = STDIN;
 $countTestCases = intval(fgets($fh));
 
 function printWin($case, $who) {
 	$winners = [NA => 'n/a', RICHARD => 'RICHARD', GABRIEL => 'GABRIEL'];
 	echo sprintf("Case #%d: %s\n", $case, $winners[$who]);
 }
 
 for ($indexCase = 0; $indexCase < $countTestCases; $indexCase += 1) {
 	list($X, $R, $C) = array_map('intval', preg_split('/\s/', fgets($fh)));
 	list($R, $C) = [min([$R, $C]), max([$R, $C])];
 
 	// var_dump(['X' => $X, 'R' => $R, 'C' => $C]);
 
 	if ($X === 1) {
 		printWin($indexCase + 1, GABRIEL);
 		continue;
 	}
 
 	if ($X > 6) {
 		printWin($indexCase + 1, RICHARD);
 		continue;
 	}
 
 	if (($R * $C) % $X !== 0) {
 		printWin($indexCase + 1, RICHARD);
 		continue;
 	}
 
 	if ($X === 2) {
 		printWin($indexCase + 1, GABRIEL);
 		continue;
 	}
 
 	if ($R === 1) {
 		printWin($indexCase + 1, RICHARD);
 		continue;
 	}
 
 	if ($X === 3) {
 		printWin($indexCase + 1, GABRIEL);
 		continue;
 	}
 
 	if ($R === 2) {
 		printWin($indexCase + 1, RICHARD);
 		continue;
 	}
 
 	if ($X <= 5) {
 		printWin($indexCase + 1, GABRIEL);
 		continue;
 	}
 
 	if ($X === 6 && $C === 4) {
 		printWin($indexCase, RICHARD);
 		continue;
 	}
 
 	printWin($indexCase + 1, GABRIEL);
 }

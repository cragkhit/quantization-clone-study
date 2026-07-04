<?php
 $input = file_get_contents($argv[1]);
 $rows = explode("\n", $input);
 
 $cases = $rows[0];
 $case = 0;
 $rows_in_case = 0;
 foreach($rows as $i => $row) {
 	if($i == 0)
 		continue;
 	if(!$rows_in_case) {
 		$case++;
 		if($case > $cases)
 			break;
 		$rows_in_case = 2 * $row - 1;
 		//$width = $row;
 		$squad = array();
 		continue;
 	}
 	$soldiers = explode(' ', $row);
 	foreach($soldiers as $soldier) {
 		if(isset($squad[$soldier]))
 			$squad[$soldier]++;
 		else
 			$squad[$soldier] = 1;
 	}
 	$rows_in_case--;
 	if($rows_in_case == 0) {
 		$missing = array();
 		foreach($squad as $soldier => $count) {
 			if($count % 2 != 0)
 				$missing[] = $soldier;
 		}
 		sort($missing);
 		$missing = implode(' ', $missing);
 		echo "Case #$case: $missing\n";
 	}
 }

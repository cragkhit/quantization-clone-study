<?php
 
 // 5
 // 2 1
 // ik
 // 3 1
 // ijk
 // 3 1
 // kji
 // 2 6
 // ji
 // 1 10000
 // i
 
 $spec   = $readrow();
 $string = $readline();
 $count  = intval($spec[1]);
 
 $multiply_two = function($chars) {
 	$chars = str_replace('1', '', $chars);
 	if (strlen($chars) === 1) {
 		return $chars;
 	}
 
 	switch ($chars) {
 		case 'ii':
 		case 'jj':
 		case 'kk':
 			return '-1';
 		case 'ij':
 			return 'k';
 		case 'ik':
 			return '-j';
 		case 'ji':
 			return '-k';
 		case 'jk':
 			return 'i';
 		case 'ki':
 			return 'j';
 		case 'kj':
 			return '-i';
 	}
 
 	throw new \Exception('Invalid two-char multiplication attempted: ' . $chars);
 };
 
 $multiply_general = function($string) use ($multiply_two) {
 	$chars = str_split($string);
 	$is_negative = false;
 
 	if ($chars[0] === '-') {
 		$is_negative = true;
 		array_shift($chars);
 	}
 
 	$current = array_shift($chars);
 
 	for ($index = 0; $index < count($chars); $index++) {
 		$current = $multiply_two($current . $chars[$index]);
 		$first_char = $current[0];
 		if ($current[0] === '-') {
 			$is_negative = ! $is_negative;
 			$current = $current[1];
 		}
 
 		if ($current === '1' && ++$index < count($chars)) {
 			$current = $chars[$index];
 		}
 	}
 
 	return $is_negative ? '-' . $current : $current;
 };
 
 $multiplied = $multiply_general($string);
 
 if (
 	($multiplied === '-1' && ($count % 2) === 1) ||
 	($multiplied !== '1' &&  ($count % 4) === 2)
 ) {
 	echo "CASE #$case:\n";
 	// This might work!
 	// Loop through until we convince ourselves it works or get all the way through
 	$current = null;
 	$is_negative = false;
 	$found_i = false;
 	$found_j = false;
 
 	for ($repetition = 0; $repetition < $count; $repetition++) {
 		for ($loop = 0; $loop < strlen($string); $loop++) {
 			$next_char = $string[$loop];
 			if (is_null($current)) {
 				$current = $next_char;
 				continue;
 			}
 
 			echo "CURRENTLY: $current\n";
 
 			if ($found_i === false) {
 				if ($current === 'i') {
 					$current = $next_char;
 					$found_i = true;
 					continue;
 				}
 			} elseif ($current === 'j') {
 				$found_j = true;
 				break 2;
 			}
 
 			if ($current[0] === '-') {
 				$current = str_replace('--', '', '-' . $multiply_two($current[1] . $next_char));
 			} else {
 				$current = $multiply_two($current . $next_char);
 			}
 		}
 	}
 
 	$result = $found_j ? 'YES' : 'NO';
 } else {
 	echo "REJECTED CASE #$case because of $multiplied and $count\n";
 	$result = 'NO';
 }

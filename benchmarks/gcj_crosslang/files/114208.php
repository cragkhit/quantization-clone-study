<?php
 
 echo "\n";
 ob_start();
 
 $in = fopen('php://stdin', 'r');
 $tmp = $cases = [];
 while(!feof($in)) {
 	$line = trim(fgets($in));
 	if(strlen($line)) {
 		if((int)$line) {
 			if($tmp) {
 				$cases[] = $tmp;
 				$tmp = [];
 			}
 		}
 		else
 			$tmp[] = $line;
 	}
 }
 fclose($in);
 $cases[] = $tmp;
 
 //print_r($cases);exit;
 
 function process($str) {
 	$out = [];
 	$last = '';
 	$count = 0;
 	$stop = strlen($str);
 	for($i = 0; $i != $stop; ++$i) {
 		$c = $str[$i];
 		if($c != $last && $count) {
 			$out[] = [$last, $count];
 			$count = 0;
 		}
 		$last = $c;
 		++$count;
 	}
 	$out[] = [$last, $count];
 	
 	return $out;
 }
 
 function simplify($in) {
 	$str = '';
 	foreach($in as $v)
 		$str .= $v[0];
 	
 	return $str;
 }
 
 function combine($processed) {
 	$out = array_fill(0, sizeof($processed[0]), []);
 	foreach($processed as $k => $v) {
 		foreach($v as $k2 => $v2) {
 			if(!isset($out[$k2][$v2[1]]))
 				$out[$k2][$v2[1]] = 0;
 			++$out[$k2][$v2[1]];
 		}
 	}
 	
 	foreach($out as $k => $v)
 		ksort($out[$k]);
 	
 	return $out;
 }
 
 function distance($place) {
 	if(sizeof($place) == 1)
 		return 0;
 	
 	$out = 0;
 	while(sizeof($place) != 1) {
 		reset($place);
 		$lowest = key($place);
 		end($place);
 		$highest = key($place);
 		
 		if($place[$lowest] < $place[$highest]) {
 			reset($place);
 			$current = current($place);
 			$nextCount = next($place);
 			$out += $current * (key($place) - $lowest);
 			$new = $place[$lowest] + $nextCount;
 			unset($place[$lowest]);
 			$place[key($place)] = $new;
 		}
 		else {
 			end($place);
 			$current = current($place);
 			$prevCount = prev($place);
 			$out += $current * ($highest - key($place));
 			$new = $place[$highest] + $prevCount;
 			unset($place[$highest]);
 			$place[key($place)] = $new;
 		}
 	}
 	
 	return $out;
 }
 
 foreach($cases as $k => $case) {
 	$poss = true;
 	$simplified = '';
 	$processed = [];
 	foreach($case as $k2 => $str) {
 		$tmp = $processed[] = process($str);
 		if($k2 && $simplified != simplify($tmp)) {
 			$poss = false;
 			break;
 		}
 		else
 			$simplified = simplify($tmp);
 	}
 	
 	if(!$poss)
 		$result = 'Fegla Won';
 	else {
 		$combined = combine($processed);
 		//print_r($combined);
 		
 		$result = 0;
 		foreach($combined as $place) {
 			$result += distance($place);
 		}
 	}
 	
 	echo 'Case #', $k + 1, ': ', $result, "\n";
 }
 
 file_put_contents('out', trim(ob_get_flush()));
 echo "\n";
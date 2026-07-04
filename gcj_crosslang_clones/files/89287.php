<?
 
 	function cin() {
 		return explode(" ", trim(fgets(STDIN)));
 	}
 
 	function last($n) {
 		if (strlen($n) == 0) return true;
 		for ($i = 0; $i < strlen($n); $i++)
 			if ($n[$i] != '9') return false;
 		return true;
 	}
 
 	function nextp($n) {
 		if ($n == '') return '';
 
 		if (strlen($n) == 1 && $n < '9') return $n+1;
 
 		$inside = substr($n, 1, strlen($n) - 2);
 		$outside = $n[0];
 
 		if (!last($inside)) {
 			return ($outside) . nextp($inside) . ($outside);
 		}
 		else {
 			if ($outside == '9') {
 				$next = '';
 				for ($i = 0; $i < strlen($n) - 1; $i++) $next .= '0';
 				return '1' . $next . '1';
 			} else {
 				$next = '';
 				for ($i = 0; $i < strlen($n) - 2; $i++) $next .= '0';
 				return ($outside + 1) . $next . ($outside + 1);
 			}
 		}
 		
 	}
 
 	function isp($n) {
 		$n .= '';
 		for ($i = 0; $i < strlen($n) / 2; $i++) {
 			if ($n[$i] != $n[strlen($n) - 1 - $i]) return false;
 		}
 		return true;
 	}
 
 	function solve($tc) {
 		list($min, $max) = cin();
 		$square = '1'; $current = '1'; $count = 0;
 		while ($square <= $max) {
 			if (isp($square) && $square >= $min) {$count++;}
 			$current = nextp($current);
 			$square = $current * $current;
 		}
 		echo "Case #$tc: $count\n";
 	}
 
 	list($tc) = cin();
 	for ($i = 0; $i < $tc; $i++)
 		solve($i + 1);
 
 ?>

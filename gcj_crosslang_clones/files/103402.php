<?
 set_time_limit(0);
 $lines = file('file.in');
 $ff = fopen("file.out","w");
 $tt = intval($lines[0]);
 $j=1;
 for($i=1;$i<=$tt;$i++){
 	$line1 = intval($lines[$j]);
 	$line2 = array_filter(explode(' ', $lines[$j+1]));
 	$j = $j+2;
 	$max_value = max($line2);
 	$min_minutes = $max_value;
 	$m = 1;
 	while ($m<=$max_value){
 			$minutes = $m;
 			foreach($line2 as $p){
 				if ($p > $m){
 					$minutes += floor($p / $m) - ($p % $m == 0 ? 1 : 0);
 				}
 			}
 			$min_minutes = min($min_minutes,$minutes);
 			$m+=1;
 	}
 	$res[] = intval($min_minutes);
 }
 $ww = 1;
 foreach($res as $value){
 	fwrite($ff, "Case #".$ww.": ".$value."\n");
 	$ww++;
 }
 ?>
<?php
 //codejam
 $input = 'http://localhost/projects/A-large.in';
 $output = 'codejamoutput.txt';
 
 $input = file_get_contents($input);
 $testCases = explode("\n",$input);
 
 $cases = $testCases[0]; //test cases
 $needed = 0;
 $gotten = 0;
 
 for($i=1; $i<=$cases; $i++){
 	$line = $testCases[$i];
 	$ln = explode(' ', $line);
 	$max_shy_level = $ln[0];
 	$shy_level = str_split($ln[1]);
 	foreach($shy_level as $level=>$people){
 		if($level == 0){
 			$gotten += $people;
 		} else {
 			if($gotten < $level){
 				$borrow = $level - $gotten;
 				$needed += $borrow;
 				$gotten += $borrow;
 				$gotten += $people;
 			} else {
 				$gotten += $people;	
 			}
 		}
 	}
 	if(isset($txt)){
 		$txt .= "Case #{$i}: {$needed} \n";
 	} else {
 		$txt = "Case #{$i}: {$needed} \n";
 	}
 	echo "Case #{$i}: {$needed}";
 	echo "<br/>";
 	$needed=0; $gotten=0;
 }
 $myfile = fopen($output, "w") or die("Unable to open file!");
 fwrite($myfile, $txt);
 
 
 ?>
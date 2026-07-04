<?php
 
 function solve($s) {
 	$letters = str_split($s);
 	//$sorted_letters = $letters;
 	//sort($letters);
 	$result = "";
 	for($i=0;$i<sizeof($letters);$i++) {
 		//if(ord($letters[$i])<ord($result))
 			$result1 = $letters[$i].$result;
 		//else
 			$result2 = $result.$letters[$i];
 		if($result1>=$result2)
 			$result = $result1;
 		else
 			$result = $result2;
 			
 	}
 	return $result;
 }
 
 /* Load data */
 $lines = file('A-large.in');
 $pos = 0;
 $cases = array();
 $numcases = trim($lines[$pos++]);
 for ($casenum = 1; $casenum <= $numcases; $casenum++) {
     $cases[$casenum] = array(
         'S' => trim($lines[$pos++])
         );
 }
 
 /* Do the work */
 $output = '';
 foreach ($cases as $casenum => $case) {
     /* Output the number of trains */
     $output .= sprintf("Case #%d: %s\r\n", $casenum, solve($case['S']));
 	
 }
 
 //output
 file_put_contents('A-large.out', $output);
 echo nl2br($output);
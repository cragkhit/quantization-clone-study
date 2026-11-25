<?php
 	// Taylor Reece, Google Code Jam
 	// Qualification Round, Problem 2
 	$debug = false;
 	error_reporting(E_ALL);
 	ini_set('display_errors', '1');
 	
 	$listOfSquares = array();
 	$squaresFile = fopen("squares.txt", "r");
 	while(!feof($squaresFile)){
 		$listOfSquares[] = preg_replace('/\r\n/', '', fgets($squaresFile));
 	}
 	fclose($squaresFile);
 	
 	function isOne($number){
 		global $listOfSquares;
 		if( in_array($number,$listOfSquares) ){
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 	
 	function squaresBelow($number){
 		global $debug;
 		global $listOfSquares;
 		$count = 0;
 		foreach($listOfSquares as $square){
 			if($square == $number){
 				return ++$count;
 			} else if(strlen($number) < strlen($square)) { // If the square is bigger, exit loop.
 				return $count;
 			}
 			
 			if(strlen($number) == strlen($square)){
 				if($square < $number){
 					$count++;
 				} else {
 					return $count;
 				}
 			} else {
 				$count++;
 			}
 		}
 		return $count;
 	}
 	
 	$inputfile = fopen("input.txt", "r");
 	$numProblems = (int)fgets($inputfile);
 
 	for($i = 0; $i < $numProblems; $i++){
 		
 		$twoNums = explode(" ",preg_replace('/\r\n/', '',fgets($inputfile)));
 		$numBetween = squaresBelow($twoNums[1]) - squaresBelow($twoNums[0]);
 		$numBetween += isOne($twoNums[0]);
 		echo "Case #" . ($i+1) . ": $numBetween\r\n";
 		
 		if($debug) echo "------------------------------\n";
 		
 	}
 	fclose($inputfile);
 	
 ?>
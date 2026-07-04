<?php
 ini_set('display_errors', 'on');
 error_reporting(E_ALL);
 set_time_limit(600);
 ini_set('memory_limit','500M');
 class Solution {	
 	
 	public $inputFile = 'C:\wamp\www\google_code_jam\data\amino.in';
 	public $outputFile = 'C:\wamp\www\google_code_jam\data\amino.out';
 	public $numInputParam = 1;	
 	
 
 	function process($caseNo, $paramArr) {
 	
 		$inArr = explode(' ', trim($paramArr[0]));
 		$x = (int)$inArr[0];
 		$r = (int)$inArr[1];
 		$c = (int)$inArr[2];		
 		
 		//var_dump($x,$r,$c);
 		
 		$richard = $this->isRWin($x, $r, $c);
 		if($richard) {
 			$out = 'RICHARD';
 		} else {
 			$out = 'GABRIEL';
 		}
 		
 		return array($out);
 		
 	}
 	
 	function isRWin($x, $r, $c) {
 		/*
 			Richard will win if :
 			1) $x >= 7. clearly seen from diagram there is one copy which contains hole in it.
 			2) if $r and $c both are < $x
 			3) if one of $r and $c < (half of $x amino L shaped)
 			4) if $r * $c is not divisible by $x
 			5) if one of $r or $c = $x and other is 2.(4 shaped amino)
 			in all other cases, richard will loose.
 		*/
 		
 		$low = ($r < $c) ? $r : $c; 
 		
 		if($x >= 7) {
 			return true;
 		}
 		
 		if($x == 1) return false;
 		
 		if($r < $x && $c < $x) {
 			return true;
 		}
 		
 		if(($r*$c) % $x != 0) {
 			return true;
 		}		
 		
 		if($low < ceil($x/2.0)) {
 			return true;
 		}
 
 		if($x > 3 && $r == $x && $c == 2) return true;
 		if($x > 3 && $c == $x && $r == 2) return true;
 		
 		return false;
 	}
 	
 	function printOutput($caseNo, $outputArr) {
 		$strOutput = "Case #".$caseNo.": ".implode(" ", $outputArr)."\n";
 		
 		$fp = fopen($this->outputFile, 'a');
 		fwrite($fp, $strOutput);
 		fclose($fp);
 	}	
 	
 	function getInput() {
 		$file = file_get_contents($this->inputFile);
 		$file = trim($file);
 		$arrInput = explode("\n", $file);
 		return $arrInput;
 	}
 	
 	function createOutputFile() {
 		file_put_contents($this->outputFile, '');
 	}
 	
 	function main2() {
 	
 		$arrInput = $this->getInput();
 		
 		$this->createOutputFile();
 		
 		$totalCases = $arrInput[0];
 		$totalInputLines = count($arrInput);
 		$case = 0; $j = -1; $totalWires = 0; $wire1Arr = $wire2Arr = array();
 		for($i=1; $i < $totalInputLines; $i++) {			
 			
 			if($j == -1) {			
 				$j = 0; $case ++;				
 				$totalWires = $arrInput[$i];
 				unset($wire1Arr, $wire2Arr);
 				$wire1Arr = $wire2Arr = array();
 				continue;
 			}
 			
 			$wArr = explode(' ', $arrInput[$i]);
 			$wire1Arr[] = (int)$wArr[0];
 			$wire2Arr[] = (int)$wArr[1];
 			$j++;
 			
 			
 			if($j == $totalWires) {
 				$outArr = $this->process($case, $totalWires, $wire1Arr, $wire2Arr);
 				$this->printOutput($case, $outArr);
 				$j = -1;
 			}
 		}
 		
 		echo 'DONE';
 	}
 	
 	function main() {
 	
 		$arrInput = $this->getInput();
 		
 		$this->createOutputFile();
 		
 		$totalCases = $arrInput[0];
 		
 		/*
 		$totalInputLine = count($arrInput);		
 		for($i=1; $i < $totalInputLine; $i++) {			
 		*/
 		$case = 1;
 		for($i=1; $case <= $totalCases; $i=$i+$this->numInputParam) {			
 			
 			$totalBlock = $arrInput[$i];
 			
 			$paramArr = array();
 			for($j=0; $j < $this->numInputParam; $j++) {
 				$paramArr[] = trim($arrInput[$i+$j]);
 			}
 			$outArr = $this->process($case, $paramArr);
 			$this->printOutput($case, $outArr);
 			$case ++;
 		}
 		echo 'DONE';
 	}
 }
 $objSolution = new Solution();
 $objSolution->main();
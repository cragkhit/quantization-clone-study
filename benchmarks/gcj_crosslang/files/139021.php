<?php
 define('L', 0);
 define('G', 1);
 
 set_time_limit (300);
 ini_set('memory_limit', '2048M');
 
 class fractiles{
 	public $T;
 	public $input;
 	public $output;
 	
 	public $convert = [0 => 'L', 1 => 'G'];
 	
 	public function init(){
 		$this->input = fopen("input.in", "r");
 		$this->output = fopen("output.txt", "w+");
 	}	
 	
 	public function out($str, $nbCase){
 		clog('Case #'.$nbCase .': ' . $str . PHP_EOL);
 		flush();
 		fwrite($this->output, 'Case #'.$nbCase .': ' . $str . PHP_EOL);
 	}
 	
 	public function getPossibleBases($K) {
 		$possibleBases = [];
 		$max = gmp_intval(gmp_pow(2, $K));
 		for($number = 0; $number < $max; $number++) {
 			$possibleBases[] = $number;
 		}
 		return $possibleBases;
 	}
 	
 	public function getPossibleArtWorks($base, $K, $C, $nextG) {
 		$base = str_pad ( decbin($base), $K, '0', STR_PAD_LEFT);
 		$t = $base;
 		clog("base: $t");
 		//$max = gmp_intval(gmp_pow(2, $K));
 		for($c = 1; $c < $C; $c++) {
 			$tmp = '';
 			for($i=0; $i < strlen($t); $i++) {
 				if($t[$i] == G) {
 					$tmp .= $nextG;
 				} else {
 					$tmp .= $base;
 				}
 			}
 			$t = $tmp;
 			clog("c: $c C $C tmp $tmp");
 		}
 		clog("artWork: $t");
 		return $t;
 	}
 	
 	public function bruteForce($K, $C) {
 		$r = [];
 		for($i=0; $i < $K; $i++) {
 			$r[] = $i * $K + $i + 1;
 		}
 		return implode(' ', $r);
 	}
 	
 	public function solveTestCase(){
 		fscanf($this->input, '%d %d %d', $K, $C, $S);
 		clog("line:  K $K, C $C, S $S");
 		if($K == 1) return 1;
 		if($C == 1) {
 			$r = [];
 			for($i=1; $i <= $K; $i++) {
 				$r[] = $i;
 			}
 			return implode(' ', $r);
 		}
 		$C = 2;
 		if($S >= $K) {
 			return $this->bruteForce($K, $C);
 		}
 		if($S < $K) return 'IMPOSSIBLE';
 		
 		$length = gmp_intval (gmp_pow($K, $C));
 		$max = gmp_intval (gmp_pow(2, $length));
 		$nextG = str_pad ( decbin(gmp_intval(gmp_pow(2, $K)) - 1), $K, '0', STR_PAD_LEFT);
 		$onlyG = str_pad ( decbin($max - 1), $length, '0', STR_PAD_LEFT);
 		clog("nextG: $nextG");
 		clog("onlyG : $onlyG");
 		//$possibleBases = $this->getPossibleBases($K);
 		//$possibleArtWorks = [];
 		
 		$possibleBases = [];
 		$max = gmp_intval(gmp_pow(2, $K));
 		$countOccL = [];
 		for($i=0 ; $i<$length; $i++) {
 			$countOccL[$i] = 0;
 		}
 		for($number = 0; $number < $max; $number++) {
 			$possibleBase = $number;
 			$possibleArtWork = $this->getPossibleArtWorks( $possibleBase, $K, $C, $nextG);
 			for($i=0 ; $i<$length; $i++) {
 				if($possibleArtWork[$i] != G) $countOccL[$i]++;
 			}
 		}
 		//clogObj($countOccL, 'occ');
 		$minValue = min($countOccL);
 		if ($minValue > $S) return 'IMPOSSIBLE';
 		asort($countOccL);
 		$r = [];
 		$i = 0;
 		foreach($countOccL as $pos => $n) {
 			if($i >= $minValue) break;
 			$r[] = $pos + 1;
 			$i++;
 		}
 		return implode(' ', $r);
 	}
 	
 	
 	public function play(){
 		fscanf($this->input, '%d', $this->T);
 		for($cptT=1; $cptT <= $this->T; $cptT++){
 			$out = $this->solveTestCase();
 			$this->out($out, $cptT);
 		}
 	}
 	
 }
 function clog($str){
 	echo $str . '<br>';
 }
 
 function clogObj($obj, $str = ''){
 	echo $str . ' ' . var_export($obj, true) . '<br>';
 }
 
 $fractiles = new fractiles();
 $fractiles->init();
 $fractiles->play();
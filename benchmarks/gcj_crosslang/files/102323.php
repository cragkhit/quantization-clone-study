<?php
 
 chdir(dirname(__FILE__));
 
 //$f = fopen('./exemple.txt', 'r');
 $f = fopen('./C-large.in', 'r');
 
 fscanf($f, '%d', $nbTests);
 for ($testNum=1; $testNum<=$nbTests; $testNum++) {
 	echo 'Case #'.$testNum.': ';
 	
 	fscanf($f, '%f %f', $len, $repeat);
 	
 	if ($repeat > 8) {
 		$repeat = 8 + fmod($repeat, 4);
 	}
 	
 	$part = trim(fgets($f));
 	$str = str_repeat($part, $repeat);
 	
 	$num = new Num($str);
 	
 	// Recherche i
 	$fail = false;
 	while (!$num->is('i')) {
 		if ($num->finish()) {
 			$fail = true;
 			break;
 		}
 		$num->consume();
 	}
 	
 	// Recherche j
 	if (!$fail) {
 		$num->next();
 		while (!$num->is('j')) {
 			if ($num->finish()) {
 				$fail = true;
 				break;
 			}
 			$num->consume();
 		}
 	}
 	
 	// k avec le reste
 	if (!$fail) {
 		$num->next();
 		while (!$num->finish()) {
 			$num->consume();
 		}
 		if (!$num->is('k')) {
 			$fail = true;
 		}
 	}
 	
 	echo $fail ? 'NO' : 'YES';
 	
 	//echo substr($str, 0, 100), "...";
 	
 	echo "\n";
 	//exit;
 }
 
 fclose($f);
 
 class Num
 {
 	protected $_ptr;
 	protected $_val;
 	protected $_neg;
 	protected $_str;
 	protected $_len;
 	
 	public function __construct($str) {
 		$this->_str = $str;
 		$this->_val = $str[0];
 		$this->_neg = 0;
 		$this->_ptr = 1;
 		$this->_len = strlen($str);
 	}
 	
 	public function consume() {
 		$v = $this->_val . $this->_str[$this->_ptr];
 		$this->_val = self::$_prod[$v];
 		if ($this->_neg) {
 			$this->_neg = 1 - self::$_negProd[$v];
 		} else {
 			$this->_neg = self::$_negProd[$v];
 		}
 		$this->_ptr++;
 	}
 	
 	public function is($c) {
 		if ($this->_neg) return false;
 		return $this->_val == $c;
 	}
 	
 	public function finish() {
 		return ($this->_ptr >= $this->_len);
 	}
 	
 	public function next() {
 		$this->_neg = 0;
 		$this->_val = $this->_str[$this->_ptr];
 		$this->_ptr++;
 	}
 	
 	protected static $_prod = array(
 		'11' => '1', '1i' => 'i', '1j' => 'j', '1k' => 'k',
 		'i1' => 'i', 'ii' => '1', 'ij' => 'k', 'ik' => 'j',
 		'j1' => 'j', 'ji' => 'k', 'jj' => '1', 'jk' => 'i',
 		'k1' => 'k', 'ki' => 'j', 'kj' => 'i', 'kk' => '1'
 	);
 	protected static $_negProd = array(
 		'11' => 0, '1i' => 0, '1j' => 0, '1k' => 0,
 		'i1' => 0, 'ii' => 1, 'ij' => 0, 'ik' => 1,
 		'j1' => 0, 'ji' => 1, 'jj' => 1, 'jk' => 0,
 		'k1' => 0, 'ki' => 0, 'kj' => 1, 'kk' => 1
 	);
 	
 }

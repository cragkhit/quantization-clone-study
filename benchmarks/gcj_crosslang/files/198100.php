<?php
 
   /**
    * NOTE(Marin): this file if run in terminal with the following command: 'php -q D-fractiles-program.php input-file.in'
    * All classes have been placed in this one file for uploading simplicity
    * @author Marin Zaimov (marin.zaimov@gmail.com)
    */
 
 
   $inputFilename = $argv[1];
   $fileInfo = pathinfo($inputFilename);
   
   $phoneNumberProblem = new PhoneNumberProblem($inputFilename, $fileInfo['filename'] . '.out');
   $phoneNumberProblem->solve();
 
   var_dump("program execution complete");
 
 
 
 
   /**
    * File to handle the Fractiles problem (D) in Google Code Jam 2016
    * Reads input file, calls FractilesTestCase to solve each test case and writes the output file
    * @param $inputFilename
    * @param $outputFilename
    * @author Marin Zaimov (marin.zaimov@gmail.com)
    */
   class PhoneNumberProblem {
 
     private $lines = [];
     private $outputFilename;
     public function __construct($inputFilename, $outputFilename) {
       $this->lines = file($inputFilename, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
       $this->outputFilename = $outputFilename;
     }
 
     public function solve() {
       $numberOfLines = array_shift($this->lines);
       
       $outputFile = fopen($this->outputFilename, "w") or die("Unable to open file!");
       
       foreach($this->lines as $index => $line) {
 
           $testCase = new PhoneNumberTestCase($line);
           $output = $testCase->getOutput();
 
           fwrite($outputFile, "Case #" . strval(intval($index)+1) . ": " . $output."\n");
       }
 
       fclose($outputFile);
     }
 
   }
 
 
 
 
 
   /**
    * FractilesTestCase processes and returns the output for one test case of the Fractiles problem (D) in Google Code Jam 2016
    * @param $numTiles (K) number of tiles in the sequence with complexity 1 
    * @param $complexity (C) levels of complexity of the test case
    * @param $students (S) number of grad students at our disposal to clean tiles
    * 
    * @author Marin Zaimov (marin.zaimov@gmail.com)
    */
   class PhoneNumberTestCase {
 
     private $numString;
     private $numbersOfDigits;
     private $orderedDigits;
 
     public function __construct($numString) {
       $this->numString = $numString;
     }
 
     public function getOutput() {
 
       $charHash = [];
       foreach (str_split($this->numString) as $index => $char) {
         if ($charHash[$char] == null) {
           $charHash[$char] = 1;
         } else {
           $charHash[$char] = $charHash[$char] + 1;
         }
       }
 
       // var_dump($charHash);
       $this->writeNumberOfNumbers($charHash);
 
       $this->orderDigits();
 
       return implode($this->orderedDigits);
     }
 
     private function writeNumberOfNumbers($charHash) {
       //ZERO
       $this->numbersOfDigits[0] = $charHash['Z'];
       $charHash['E'] = $charHash['E'] - $charHash['Z'];
       $charHash['R'] = $charHash['R'] - $charHash['Z'];
       $charHash['O'] = $charHash['O'] - $charHash['Z'];
 
       //TWO
       $this->numbersOfDigits[2] = $charHash['W'];
       $charHash['O'] = $charHash['O'] - $charHash['W'];
       $charHash['T'] = $charHash['T'] - $charHash['W'];
 
       //FOUR
       $this->numbersOfDigits[4] = $charHash['U'];
       $charHash['F'] = $charHash['F'] - $charHash['U'];
       $charHash['O'] = $charHash['O'] - $charHash['U'];
       $charHash['R'] = $charHash['R'] - $charHash['U'];
 
       //SIX
       $this->numbersOfDigits[6] = $charHash['X'];
       $charHash['S'] = $charHash['S'] - $charHash['X'];
       $charHash['I'] = $charHash['I'] - $charHash['X'];
 
       //EIGHT
       $this->numbersOfDigits[8] = $charHash['G'];
       $charHash['E'] = $charHash['E'] - $charHash['G'];
       $charHash['I'] = $charHash['I'] - $charHash['G'];
       $charHash['H'] = $charHash['H'] - $charHash['G'];
       $charHash['T'] = $charHash['T'] - $charHash['G'];
 
       //ONE
       $this->numbersOfDigits[1] = $charHash['O'];
       $charHash['N'] = $charHash['N'] - $charHash['O'];
       $charHash['E'] = $charHash['E'] - $charHash['O'];
 
       //THREE
       $this->numbersOfDigits[3] = $charHash['R'];
       $charHash['T'] = $charHash['T'] - $charHash['R'];
       $charHash['H'] = $charHash['H'] - $charHash['R'];
       $charHash['E'] = $charHash['E'] - $charHash['R'];
       $charHash['E'] = $charHash['E'] - $charHash['R'];
 
       //FIVE
       $this->numbersOfDigits[5] = $charHash['F'];
       $charHash['I'] = $charHash['I'] - $charHash['F'];
       $charHash['V'] = $charHash['V'] - $charHash['F'];
       $charHash['E'] = $charHash['E'] - $charHash['F'];
 
       //SEVEN
       $this->numbersOfDigits[7] = $charHash['S'];
       $charHash['N'] = $charHash['N'] - $charHash['S'];
       $charHash['V'] = $charHash['V'] - $charHash['S'];
       $charHash['E'] = $charHash['E'] - $charHash['S'];
       $charHash['E'] = $charHash['E'] - $charHash['S'];
 
 
       //NINE
       $this->numbersOfDigits[9] = $charHash['I'];
       $charHash['N'] = $charHash['N'] - $charHash['I'];
       $charHash['N'] = $charHash['N'] - $charHash['I'];
       $charHash['E'] = $charHash['E'] - $charHash['I'];
 
       // var_dump($charHash);
       // var_dump($this->numbersOfDigits);
     }
 
     private function orderDigits() {
       $this->orderedDigits = [];
       for ($digit = 0; $digit < 10; $digit++) {
         if ($this->numbersOfDigits[$digit] != null && $this->numbersOfDigits[$digit] != 0) {
           for ($i = 1; $i <= $this->numbersOfDigits[$digit]; $i++) {
             $this->orderedDigits[] = $digit + "";
           }
         }
       }
       // var_dump($this->orderedDigits);
     }
 
   }
 
 
 ?>
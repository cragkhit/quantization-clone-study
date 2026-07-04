<?php
 
 function t($i) 
 { 
 	$a = array( '1', 'i', 'j', 'k' );
 	if ($i == "1") return "1";
 	if (($r = array_search($i, $a)) !== FALSE) 
 	{
 		return $r + 1;
 	}
 	if ($i < 0) { return "-".$a[-1*$i-1]; } 
 	return $a[$i-1];
 }
 
 function mult($a, $b) 
 {
 	$ar = array( 
 		array(  1,  2,  3,  4 ), 
 		array(  2, -1,  4, -3 ), 
 		array(  3, -4, -1,  2 ), 
 		array(  4,  3, -2, -1 )
 		);
 	
 	if (($a * $b) < 0) { 
 		$mult = -1;
 	} else { 
 		$mult = 1;
 	}
 
 	return ($mult * ($ar[(abs($a))-1][(abs($b))-1]));
 }
 
 function do_test($io, $case) { 
   global $io, $s;
   $l = $io->read();
   $x = $io->read();
   $s = $io->read();
   
   if ($l * $x < 3) { return "NO"; } 
   $io->debug("\n\nL=$l, X=$x => $s\n\n");
   
   /*
   There has to be a trick to this, i * j * k = -1, so the whole 
   string must equate to that. If there's a way to make an i, then 
   a j, then a k then all you need to ensure is that there's a way 
   to make an i, then a k (=i*j) that a -1 (=i*j*k=k*k). 
   Meh, let's try it ...
   Welp, that passed small so I'm right. 
   Therefore if the result of the repeated phrase, repeated doesn't 
   match -1 then NO. 
   Still need to find the mid-points -- look for them on the way 
   through the first turn. If found, and -1 -- ship it!
   Otherwise there are only eight states, if we hit any state we've 
   already been in then it can't be done. 
   
   */
   $a = t($s[0]);
   $i = 1; $target = array(2,4); 
   // Pass one ... 
   while ($i <= ($l))
   {
 	if (count($target) && ($a == $target[0]))
 	{ 
 		$io->debug("FOUND ".t($target[0])."\n");
 		array_shift($target);
 	}
 	if ($i < ($l)) 
 	{
 	  $io->debug(str_pad($i, 5, " ", STR_PAD_LEFT).": $a (".t($a).")");
 	  $b = t($s[$i]); 
 	  $io->debug(" * $b (".t($b).") = ");
 	  $a = mult($a, $b);
 	  $io->debug(" $a (".t($a).")\n");
 	}
 	$i ++;
   } 
   // Check if we're done ... 
   if ((count($target) == 0) && ($a == -1))
   {
 	return "YES";
   } 
   // Sanity check that we'll get to '-1' 
   // if we're -1 check that there's an odd number 
   // of repeats, otherwise we can't get to '-1'
   // unless we're repeating i, j or k an even 
   // number of times
   // So valid is '-1' and odd X, or i,j,k and even 
   // x. 
   if (($x % 2) == 0) // Even
   { 
 	if (($a != 2) && ($a != 3) && ($a != 4)) 
 	{ 
 		return "NO"; // Can't be done
 	}
   } 
   else  // Odd
   {
     if ($a != -1) 
 	{
       return "NO"; // Can't be done
     }
   }
   
   // Nope, need to look further
   $found = array($a);
   $x --;
   if (!$x) { return "NO"; } 
   do 
   { 
     $i = 1;
     while ($i <= ($l))
     {
       if (count($target) && ($a == $target[0]))
 	  {   
 		$io->debug("FOUND ".t($target[0])."\n");
 		array_shift($target);
 	  }
 	  if ($i < ($l)) 
 	  {
 	    $io->debug(str_pad($i, 5, " ", STR_PAD_LEFT).": $a (".t($a).")");
 	    $b = t($s[$i]); 
 	    $io->debug(" * $b (".t($b).") = ");
 	    $a = mult($a, $b);
 	    $io->debug(" $a (".t($a).")\n");
 	  }
 	  $i ++;
     }
 	if (count($target) == 0) 
 	{
 		// Valid combo
 		return "YES";
 	}
 	if (in_array($a, $found)) 
 	{ 
 		// Loop
 		return "NO";
 	}
 	else 
 	{
 		array_push($found, $a);
 	}
 	$x --;
   }	
   while ($x); 
   return "NO";
   
 }
 
 $io = new IO();
 
 if ($io->is_debug()) 
 {
   echo "      | ";
   for ($i = 1; $i < 5; $i ++) { 
     echo " ".t($i). " | ";
   }
   echo "\n";
   echo "      ~ ";
   for ($i = 1; $i < 5; $i ++) { 
     echo " ".t(t($i)). " ~ ";
   }
   echo "\n";
 
   for ($i = 1; $i < 5; $i ++) { 
     echo t($i). " ~ ".t(t($i))." ~ ";
     for ($j = 1; $j < 5; $j ++) { 
 	  echo str_pad(t(mult($i, $j)) . " | ", 5, " ", STR_PAD_LEFT);
     }
     echo "\n";
   } 
 }
 
 // Read the problems 
 
 $num_entries = $io->read();
 $case = 1;
 do 
 {
   $io->answer($case, do_test($io, $case));
 } 
 while (($case++) < $num_entries);
 
 
 return 0;
 
 // General class to handle input output 
 class IO { 
   private $S = NULL;
   private $a = array();
   private $debug = false;
   private function r() {
     if (empty($this->a)) {$this->a = explode(" ",trim(fgets($this->S)));}
   }
   function __construct() {
     $this->S = fopen("php://stdin", "r"); 
     global $argv, $argc; if (($argc > 1) && ($argv[1] == '-d')) { $this->debug = true; } 
   }
   function read()              { $this->r(); return array_shift($this->a); }
   function readline_a()        { $this->r(); $r = $this->a; $this->a = array(); return $r; }
   function readline()          { return implode(" ", $this->readline_a()); }
   function answer($case, $str) { echo "Case #$case: $str \n"; }
   function debug($str)         { if ($this->debug) { echo "$str"; } }
   function debug_r($a)         { if ($this->debug) { print_r($a); } }
   function is_debug()          { return $this->debug; } 
 }
  
 ?>
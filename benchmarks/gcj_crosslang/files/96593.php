<?php
 
 function getInput()
 {
     $input = file('input.txt');
     unset($input[0]);
 
     return $input;
 }
 
 function getNeededFriend($case)
 {
     $visitors = str_split(substr($case, 2));
     array_pop($visitors);
 
     $neededFriend = 0;
 
     do {
 
         if (isEveryBodyStanding($visitors)) break;
 
         $neededFriend++;
 
         addAFriend($visitors);
 
     } while (true);
 
     return $neededFriend;
 }
 
 function isEveryBodyStanding($visitors)
 {
     $totalClapping = 0;
     $totalVisitors = 0;
 
     foreach ($visitors as $shyLevel => $visitor)
     {
         $totalVisitors += intval($visitor);
 
         if ($visitor != 0 && ($shyLevel <= $totalClapping || $shyLevel == 0))
         {
             $totalClapping += intval($visitor);
         }
     }
 
     return $totalClapping == $totalVisitors;
 }
 
 
 function addAFriend(&$visitors)
 {
     foreach ($visitors as $shyLevel => $visitor)
     {
         if ($visitor == 0)
         {
             $visitors[$shyLevel] = 1;
             break;
         }
     }
 }
 
 $input  = getInput();
 $output = '';
 
 foreach ($input as $testCase => $case) {
     $output .= sprintf("Case #%d: %d\n", $testCase, getNeededFriend($case));
 }
 
 file_put_contents('output.txt', $output);
 echo 'Done';
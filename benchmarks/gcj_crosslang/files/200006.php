<?php
 $fp = fopen("1.in", "r");
 $fr = fopen("1.out", "w");
 $N = intval(chop(fgets($fp)));
 for ($i = 1; $i <= $N; ++$i) {
     $res = solve(chop(fgets($fp)));
     fputs($fr, "Case #{$i}: {$res}\n");
 }
 
 fclose($fr);
 fclose($fp);
 
 function solve($str)
 {
     $letters = [];
     $ans = [];
     for ($i = 0; $i < strlen($str); ++$i) {
         if (isset($letters[$str[$i]])) {
             $letters[$str[$i]]++;
         }else {
             $letters[$str[$i]] = 1;
         }
     }
     if (isset($letters['Z'])) {
         $ans[0] = 0;
         $c = $letters['Z'];
         for ($i = 0; $i < $c; ++$i){
             $ans[0]++;
             $letters['Z']--;
             $letters['E']--;
             $letters['R']--;
             $letters['O']--;
         }
     }
     if (isset($letters['W'])) {
         $ans[2] = 0;
         $c = $letters['W'];
         for ($i = 0; $i < $c; ++$i){
             $ans[2]++;
             $letters['T']--;
             $letters['W']--;
             $letters['O']--;
         }
     }
     if (isset($letters['U'])) {
         $ans[4] = 0;
         $c = $letters['U'];
         for ($i = 0; $i < $c; ++$i){
             $ans[4]++;
             $letters['F']--;
             $letters['O']--;
             $letters['U']--;
             $letters['R']--;
         }
     }
     if (isset($letters['X'])) {
         $ans[6] = 0;
         $c = $letters['X'];
         for ($i = 0; $i < $c; ++$i){
             $ans[6]++;
             $letters['S']--;
             $letters['I']--;
             $letters['X']--;
         }
     }
     if (isset($letters['G'])) {
         $ans[8] = 0;
         $c = $letters['G'];
         for ($i = 0; $i < $c; ++$i){
             $ans[8]++;
             $letters['E']--;
             $letters['I']--;
             $letters['G']--;
             $letters['H']--;
             $letters['T']--;
         }
     }
     if (isset($letters['O'])) {
         $ans[1] = 0;
         $c = $letters['O'];
         for ($i = 0; $i < $c; ++$i){
             $ans[1]++;
             $letters['O']--;
             $letters['N']--;
             $letters['E']--;
         }
     }
     if (isset($letters['T'])) {
         $ans[3] = 0;
         $c = $letters['T'];
         for ($i = 0; $i < $c; ++$i){
             $ans[3]++;
             $letters['T']--;
             $letters['H']--;
             $letters['R']--;
             $letters['E']--;
             $letters['E']--;
         }
     }
     if (isset($letters['F'])) {
         $ans[5] = 0;
         $c = $letters['F'];
         for ($i = 0; $i < $c; ++$i){
             $ans[5]++;
             $letters['F']--;
             $letters['I']--;
             $letters['V']--;
             $letters['E']--;
         }
     }
     if (isset($letters['S'])) {
         $ans[7] = 0;
         $c = $letters['S'];
         for ($i = 0; $i < $c; ++$i){
             $ans[7]++;
             $letters['S']--;
             $letters['E']--;
             $letters['V']--;
             $letters['E']--;
             $letters['N']--;
         }
     }
     if (isset($letters['N'])) {
         $ans[9] = 0;
         $c = $letters['N'];
         for ($i = 0; $i < $c; $i += 2){
             $ans[9]++;
             $letters['N']--;
             $letters['I']--;
             $letters['N']--;
             $letters['E']--;
         }
     }
     $res = '';
     for ($i = 0; $i < 10; ++$i) {
         if (isset($ans[$i])) {
             for ($j = 0; $j < $ans[$i]; ++$j) {
                 $res .= $i;
             }
         }
     }
     foreach ($letters as $l) {
         if ($l != 0) {
             echo $str."\n";
             break;
         }
     }
     return $res;
 }
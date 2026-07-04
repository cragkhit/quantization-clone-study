<?php
 $handle = @fopen('B-large-input.in', 'r');
 $handle2 = @fopen('B-large-output.out', "w+");
 $T = trim(fgets($handle));
 for($i = 0; $i < $T; $i++)
 {
     $line = trim(fgets($handle));
 //    $lineParts = explode(' ', $line);
 //    $N = $lineParts[0];
     $N = $line;
 
     $lists = array();
     $missing = array();
     for($j = 0; $j < (2*$N - 1); $j++)
     {
         $line = trim(fgets($handle));
         $lists[] = explode(' ', $line);
         for($k = 0; $k < $N; $k++)
         {
             if(!isset($missing[$lists[$j][$k]]))
             {
                 $missing[$lists[$j][$k]] = 1;
             }
             else
             {
                 $missing[$lists[$j][$k]] = ($missing[$lists[$j][$k]] + 1) % 2;
             }
         }
     }
     $missing = array_filter($missing, function ($var){
         return $var == 1;
     });
     $res = array_keys($missing);
     sort($res);
     
     echo $i . ' -> ' . implode(' ', $res) . '<br/>';
 
     fputs($handle2, 'Case #' . ($i + 1) . ': ' . implode(' ', $res) . PHP_EOL);
 }
 fclose($handle);
 fclose($handle2);

<?php
 function read_case(&$fp)
 {
     list($rows, $columns) = explode(" ", fgets($fp));
     $arr = array();
     for($i = 0; $i < $rows; $i++)
     {
         $row = explode(" ", fgets($fp));
         for($j = 0; $j < $columns; $j++)
         {
             $arr[$i][$j] = (int)$row[$j];
         }
     }
     return array('rows' => (int)$rows, 'columns' => (int)$columns, 'arr' => $arr);
 }
 function evaluate($case)
 {
     $rows = $case['rows'];
     $columns = $case['columns'];
     $arr = $case['arr'];
 
     $max_per_row = array();
     for($i = 0; $i < $rows; $i++)
     {
         $max = 0;
         for($j = 0; $j < $columns; $j++)
         {
             $max = max($arr[$i][$j], $max);
         }
         $max_per_row[$i] = $max;
     }
 
     $max_per_column = array();
     for($j = 0; $j < $columns; $j++)
     {
         $max = 0;
         for($i = 0; $i < $rows; $i++)
         {
             $max = max($arr[$i][$j], $max);
         }
         $max_per_column[$j] = $max;
     }
 
     for($i = 0; $i < $rows; $i++)
     {
         for($j = 0; $j < $columns; $j++)
         {
             if ($max_per_column[$j] > $arr[$i][$j] && $max_per_row[$i] > $arr[$i][$j])
             {
                 return "NO";
             }
         }
     }
     return "YES";
 }
 
 $fp = fopen("B-large.in", "r");
 $fr = fopen("output.txt", "w");
 $cases = fgets($fp);
 for($i = 1; $i <= $cases; $i++)
 {
     $case = read_case($fp);
     $result = evaluate($case);
     fputs($fr, "Case #$i: $result\n");
 }
 fclose($fp);
 fclose($fr);

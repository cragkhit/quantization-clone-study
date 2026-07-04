#!/usr/bin/php
 <?php
 $filename = $_SERVER['argv'][1];
 $lines = split("\n", trim(file_get_contents($filename)));
 //print_r($lines);
 $curr_line = 1;
 for ($i=0; $i<$lines[0]; $i++){
     $case_num = $i + 1;
     $items = split(" ", trim($lines[$curr_line]));
 //    print_r($dim);
 //    $curr_line += $dim[0] + 1;
     $res = process($items[0], $items[1], 0, $items[1]);
 //    var_dump($res);
     $curr_line += 1;
     echo "Case #$case_num: $res\n";
 }
 
 function process($r, $t, $start, $end){
        if($end < 100){
 //           var_dump($end);
            for ($x = $start; $x <=$end; $x++){
                 $vol = 2*$x*$x -$x + 2*$x*$r; 
 //                echo "vol= $vol\n";
                 $vol1 = 2*($x+1)*($x+1) - ($x+1) + 2 * ($x+1) *$r;
 //                echo "vol1= $vol1\n";
                 if ($vol <= $t && $vol1 > $t){
                     return floor($x);
                 }
            }
        }
 //       exit(0);
        $x = floor(($start + $end)/2);
        $vol = 2*$x*$x -$x + 2*$x*$r; 
        $vol1 = 2*($x+1)*($x+1) - ($x+1) + 2 * ($x+1) *$r;
 //       var_dump($vol);
        if ($vol <= $t && $vol1 > $t){
 //           echo "x="; var_dump($x);
            return floor($x);
        }
        else if ($vol < $t){
            return process ($r, $t, $x, $end);
        }
        else{
            return process ($r, $t, $start, $x);
        }
 }
 
 
 
 
 ?>

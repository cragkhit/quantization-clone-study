<?php
   fscanf(STDIN, "%d", $nb);
   for ($i=0; $i < $nb; $i++) {
     fscanf(STDIN, "%d", $n);
     $tab = [];
     for ($j=0; $j < ($n * 2 - 1); $j++) {
       $a = fgets(STDIN);
       $tab = array_merge($tab, explode(" ", $a));
     }
     $tab = array_map("intval", $tab);
     $tab = array_count_values($tab);
     $f = [];
     foreach ($tab as $k => $v) {
       if ($v % 2)
         $f[] = $k;
     }
     asort($f);
     $i += 1;
     echo "Case #$i:";
     $i -= 1;
     foreach ($f as $k => $v) {
       echo " $v";
     }
     echo "\n";
   }

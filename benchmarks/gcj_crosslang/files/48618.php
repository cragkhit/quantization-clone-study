<?php
 $max = 1000000;
 fscanf( STDIN, "%d\n", $cases );
 for ( $i = 1; $i <= $cases; $i++ ) {
     $N = trim( fgets( STDIN ) );
     $digits = array();
     $count = 0;
     $NN = $N;
     while ( $count < $max ) {
         $count++;
         $NN = $N * $count;
         $aux = $NN;
         while ( $aux > 0 ) {
             $digit = $aux % 10;
             $digits[$digit] = true;
             $aux = intVal( $aux / 10 );
         }
         if ( sizeOf( $digits ) == 10 ) {
             printf( "Case #%d: %d\n", $i, $NN );
             break;
         }
     }
     if ( $count == $max ) {
         printf( "Case #%d: INSOMNIA\n", $i );
     }
 }
 ?>
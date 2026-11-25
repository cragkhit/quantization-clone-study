<?php
 fscanf( STDIN, "%d\n", $cases );
 for ( $i = 1; $i <= $cases; $i++ ) {
     $S = trim( fgets( STDIN ) );
     $length = strLen( $S );
     $lastWord = $S[0];
     for ( $k = 1; $k < $length; $k++ ) {
         if ( $S[$k] >= $lastWord[0] ) {
             $lastWord = $S[$k] . $lastWord;
         } else {
             $lastWord = $lastWord . $S[$k];
         }
     }
     printf( "Case #%d: %s\n", $i, $lastWord );
 }
 
 ?>
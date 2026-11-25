<?php
     $input=file("A-small-attempt1.in");
     for($i=1;$i<=$input[0];$i++){
     	$case=explode("/", $input[$i]);
         $Elveness=$case[0]/$case[1];
         /*
         $answer=0; //0 - if she (herself) if full blooded Elve.
         if($Elveness%(1/pow(2,40))!=0)
         	$answer="impossible";
         while(pow(1/2,$answer)>$Elveness&&$answer!="impossible"){
         	$answer++;
         }
         */
         $answer=-1;// נניח no answer
         for($j=0;$j<41;$j++){
         	if(pow(0.5, $j)<=$Elveness){
 	        	$Elveness-=pow(0.5, $j);
 	        	if( $answer==-1){
 	        		//$answer=-log($Elveness,2)-1;
 	        		$answer=$j;
 	        	}
         	}
         }
         if($Elveness*pow(2,40)!=1 && $Elveness!=0 ) $answer="impossible";
         echo "Case #" . $i . ": " . $answer . "\n";
     }
 ?>
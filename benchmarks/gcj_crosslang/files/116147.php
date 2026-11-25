<?php
 
 
 
 $in=fopen("inA.txt","r");
 $out=fopen("outA.txt","w+");
 
 
 
 $T=intval(fgets($in));
 for($kas=1;$kas<=$T;$kas++){
 	$N=intval(fgets($in));
 	for($i=0;$i<$N;$i++) $data[$i]=trim(fgets($in));
 	$ans=0;
 	
 	$possible=true;
 	for($i=0;$i<$N;$i++) $cur[$i]=0;
 	while($cur[0]<strlen($data[0]) and $possible){
 		$tmp=$data[0][$cur[0]];
 		for($i=0;$i<$N;$i++) if($data[$i][$cur[$i]]!=$tmp) $possible=false;
 		if($possible){
 			$tc=0;
 			for($i=0;$i<$N;$i++) $sav[$i]=0;
 			for($i=0;$i<$N;$i++) while($cur[$i]<strlen($data[$i]) and $data[$i][$cur[$i]]==$tmp){ $sav[$i]++; $cur[$i]++; $tc++; }
 			for($i=0;$i<$N;$i++) $ans+=abs($sav[$i]-round($tc/$N));
 			
 			echo "$tc  $ans<br>";
 		}
 	}
 	for($i=0;$i<$N;$i++) if($cur[$i]<strlen($data[$i])) $possible=false;
 	
 	
 	if(!$possible){
 		echo "Case #$kas: Fegla Won<br>";
 		fwrite($out,"Case #$kas: Fegla Won\n");
 	}
 	else{
 		echo "Case #$kas: $ans<br>";
 		fwrite($out,"Case #$kas: $ans\n");
 	}
 	
 //echo "Case #$kas: $ans<br>";
 //	fwrite($out,"Case #$kas: $ans\n");
 }
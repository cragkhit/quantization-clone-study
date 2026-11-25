<?php
 
 function gcd($A,$B){ return ($B)?gcd($B,$A%$B):$A; }
 
 
 
 
 $in=fopen("inA.txt","r");
 $out=fopen("outA.txt","w+");
 
 
 $T=intval(fgets($in));
 for($kas=1;$kas<=$T;$kas++){
 	$data=explode("/",trim(fgets($in)));
 	$P=intval($data[0]);
 	$Q=intval($data[1]);
 	
 	$tmp=gcd($P,$Q);
 	$P/=$tmp;
 	$Q/=$tmp;
 	
 	$tmp=$Q;
 	while($tmp%2==0) $tmp/=2; 
 	if($tmp!=1) $ans="impossible";
 	else{
 		$tmp=$P/$Q;
 		$ans=1;
 		while($tmp<1/2){ $ans++; $tmp*=2; }
 	}
 	
 
 	
 	echo "Case #$kas: $ans<br>";
 	fwrite($out,"Case #$kas: $ans\n");
 }
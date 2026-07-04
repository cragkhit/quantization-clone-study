<?php
 	$start=microtime(true);
 	if(ISSET($argv[1]) and ISSET($argv[2])){
 		echo "Procesing input file $argv[1] to output file $argv[2].\n";
 		$ifile = fopen($argv[1], "r" );
 		$ofile = fopen($argv[2], "w" );
 
 		if( $ifile == false ) {
 			echo "Error in opening file.\n";
 			exit();
 		}else{
 			$l=stream_get_line($ifile, 4, "\n");
 			echo "Procesing $l lines.\n";
 			for($ln=1;$ln<=$l;$ln++) {
 				$c=stream_get_line($ifile, 1001, "\n");
 				if(strlen($c)>0){
 					fwrite($ofile, calcular($c, $ln));
 				}
 			}
 			fclose($ifile);
 		}
 	}else{
 		echo "No input or output files declared.\n";
 		echo "Try: php $argv[0] input-file.php output-file.php\n";
 	}
 	$end=microtime(true);
 	$delta=intval(($end - $start)*100000)/100;
 	echo "Procesed in $delta seconds.\n";
 	
 	
 	function calcular($s, $l){
 		$s = str_split($s);
 		$t = "";
 		for($i=0;$i<count($s);$i++){
 			if(strcasecmp($t.$s[$i],$s[$i].$t)>=0){
 				$t=$t.$s[$i];
 			}else{
 				$t=$s[$i].$t;
 			}
 		}
 		return "Case #$l: $t\n";
 	}
 ?>

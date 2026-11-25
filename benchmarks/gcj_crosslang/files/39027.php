<?php
 function readMsg($filename) {
 	$dt=[];
 	$filename.='.in';
 	$myfile = fopen($filename, "r") or die("Unable to open file!");
 	//ganti pakai explode
 	while(!feof($myfile)) {
 		$dt[]= fgets($myfile);
 	}
 	fclose($myfile);
 	//$dt = array_values(array_filter($dt, "trim"));
 	return $dt;
 }
 function writeMsg($filename,$txt){
 	$filename.='.out';
 	$myfile = fopen($filename, "w") or die("Unable to open file!");
 	fwrite($myfile, $txt);
 	fclose($myfile);
 }
 /**
 $_out=substr_count($dataset,'0');
 
 */
 //Problem A. Standing Ovation
 //$file_name="A-large-practice";
 $file_name="A-large";
 $dt=readMsg($file_name);
 $test_case=$dt[0];array_shift($dt);
 $out=[];
 
 	for($i=0;$i<$test_case;$i++){
 		$case=explode(' ',trim($dt[$i]));
 		$_out='';
 		$case_amount=$case[0];
 		$dataset=$case[1];
 		
 		$ar=[0,1,2,3,4,5,6,7,8,9];
 		
 		$char='';
 		$sum=0;
 		$amount=$dt[$i];
 		$amount=(x($dt[$i],$ar,0));
 		$_out=$amount;
 		
 		$_out='Case #'.($i+1).': '.$_out;
 		echo $case_amount.'-'.$dt[$i].'<br>'.$_out.'<hr>';
 		$out[]=$_out;
 	}
 writeMsg($file_name,implode(PHP_EOL, $out));
 
 function x($x,$ar,$ijk,$s){
 	if($s==null){$s=$x;}//echo $ijk.' '.$x.' '.$s.' '.json_encode($ar).'<br>	';
 	if($x==0) return 'INSOMNIA';
 	//if($x==1)return 10;
 	foreach($ar as $key=>$a){
 		$locate=strpos($x,''.$a);
 		//var_dump($locate);var_dump($x);var_dump(''.$a);echo '<hr>';
 		if($locate!==false){//var_dump($ar);
 			unset($ar[$key]);
 		}
 	}
 	if(sizeof($ar)==0){
 		return $x;
 	}else{
 		//if($ijk<=21)
 			return x($x+=$s,$ar,$ijk+=1,$s);
 		//else
 //			return $x;
 	}
 }
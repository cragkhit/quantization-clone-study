<?php
 $input = $argv[1];
 if(!$argv[1])
 {
         echo "Syntax : Ominous_omino.php <Input file name> > <Output file name>";
        # exit;
 }
 #echo $input ."\n";
 
 $file_content = file_get_contents($input);
 #echo $file_content;
 $main_arr = Array();
 $main_arr =  explode("\n",$file_content);
 $problem_statement = Array();
 $count = 0;
 $metrix = 0;
 $omino_size = 0;
 $size_after_richard_omino = 0;
 $case_count = 1;
 foreach($main_arr as $statement)
 {
 	$problem_statement = explode(" ",$statement);
 #	print_r($problem_statement);
 	$count = count($problem_statement);
 	if($count>1)
 	{
 		$row = $problem_statement[1];
 		$column = $problem_statement[2];
 		$metrix = $problem_statement[1] * $problem_statement[2];
 		$omino_size = $problem_statement[0];
 		$size_after_richard_omino = $metrix - $omino_size;
 
 #		$res = $omino_size / 2;
 			
 		if(($omino_size>$row && $omino_size>$column) || (($omino_size == $row && $omino_size / 2 >=$column && $omino_size>2) || ($omino_size == $column && $omino_size / 2 >=$row && $omino_size>2)))
 		{
 			echo "Case #$case_count: RICHARD\n";
 			$case_count++;
 		}
 		elseif((($row * $column) % $omino_size < $omino_size) && $row * $column % $omino_size > 0)
 		{
 			echo "Case #$case_count: RICHARD\n";
 			$case_count++;	
 		}
 		else
 		{
 			echo "Case #$case_count: GABRIEL\n";
 			$case_count++;
 		}
 
 	}
 }
 
 
 
 ?>

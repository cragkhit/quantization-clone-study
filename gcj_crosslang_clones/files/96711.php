<?php
 	$input = fopen ("php://stdin","r");
 
 	$testcase = fgets($input);
 
 	for($j=1; $j<=$testcase; $j++)
 	{
 		$result = "";
 		$inputline = trim(fgets($input), "\n");
 
 		$row;
 		$col;
 
 		list($row, $col) = sscanf ($inputline, "%d %d");
 
 		$lawn = array();
 
 		for ($i=0; $i<$row; $i++)
 		{
 			$inputline = trim(fgets($input), "\n");
 			$value = explode(" ", $inputline);
 			$lawn[$i] = $value;
 		}
 
 		$fail = false;
 		
 		for ($i=0; $i<$row && !$fail; $i++)
 		{
 			for ($e=0; $e<$col && !$fail; $e++)
 			{
 				$x = $lawn[$i][$e];		
 				
 				$failOnce = false;
 				
 				for ($ii=0; $ii<$row && !$failOnce; $ii++)
 				{
 					if ($lawn[$ii][$e] > $x)
 					{						
 						$failOnce = true;
 					}
 				}	
 
 				for ($ee=0; $ee<$col && $failOnce && !$fail; $ee++)
 				{
 					if ($lawn[$i][$ee] > $x)				
 					{
 						$fail = true;
 					}
 				}				
 			}
 		}
 
 		if (!$fail)
 		{
 			echo "Case #" . $j . ": YES\n";
 		}
 		else
 		{
 			echo "Case #" . $j . ": NO\n";
 		}
 	}
 ?>
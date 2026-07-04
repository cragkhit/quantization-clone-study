<?php
 
 $data = file($argv[1], FILE_IGNORE_NEW_LINES);
 
 $testcase = $data[0];
 $nbline = 1;
 
 for($tcase = 1; $tcase <= $testcase; $tcase++)
 {
 	echo "Case #$tcase: ";
 
 	// read square
 	$line = "";
 	
 	for ($i = 0; $i < 4; ++$i)
 	{
 		$line .= $data[$nbline++];
 	}
 
 	$nbline++;
 
 	$o = $x = $p = 0;
 
 	if (preg_match("/(^(....)*[OT]{4})|([OT]...[OT]...[OT]...[OT])|([OT]....[OT]....[OT]....[OT])|(^...[OT]..[OT]..[OT]..[OT])/", $line))
 		$o = 1;
 	
 	if (preg_match("/(^(....)*[XT]{4})|([XT]...[XT]...[XT]...[XT])|([XT]....[XT]....[XT]....[XT])|(^...[XT]..[XT]..[XT]..[XT])/", $line))
 		$x = 1;
 
 	if (preg_match("/\./", $line))
 		$p = 1;
 
 	
 
 	if ($o && $x)
 		echo "Draw\n";
 	else if ($o)
 		echo "O won\n";
 	else if ($x)
 		echo "X won\n";
 	else if ($p)
 		echo "Game has not completed\n";
 	else 
 		echo "Draw\n";
 }
 
 
 ?>
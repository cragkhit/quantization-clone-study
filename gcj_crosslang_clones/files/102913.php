<?php
 	$handle = fopen('input.txt', 'r');
 	$input = fread($handle, filesize('input.txt'));
 	fclose($handle);
 
 	$tests = explode("\n", $input);
 	$tests = $tests[0];
 
 	$cases = explode("\n\n", $input);
 	$cases[0] = str_replace($tests."\n", "", $cases[0]);
 
 	$result = array("Draw", "Game has not completed", "X won", "O won");
 	$lines = array();
 	for($i = 0; $i < $tests; $i++) {
 		$lines[] = "Case #".($i+1).": ".$result[run_test($cases[$i])]."\n";
 	}
 
 	$handle = fopen('output.txt', 'w+');
 	foreach($lines as $line) {
 		fwrite($handle, $line);
 	}
 	fclose($handle);
 
 	function run_test($input) {
 		$input = explode("\n", $input);
 
 		$board = array();
 		foreach($input as $in) {	
 			$board[] = str_split($in);
 		}
 
 		$status[] = check_rows($board);
 		$status[] = check_cols($board);
 		$status[] = check_diag($board);
 
 		if(in_array("3", $status)) return 3;
 		if(in_array("2", $status)) return 2;
 		if(in_array("1", $status)) return 1;
 		else return 0;
 	}
 
 	function check_rows($board) {
 		$dotflag = 0;
 		foreach($board as $row) {
 			$status = 0;
 			$winner = -1;
 			foreach($row as $field) {
 				if($field == "T") $status++;
 				else if(in_array($field, array("O", "X"))){
 					if(in_array($winner, array(-1, $field))) {
 						$winner = $field;
 						$status++;
 					}
 				}
 				else if($field == ".") $dotflag = 1;
 			}
 			if($status == 4) {
 				if($winner == "X") return 2;
 				if($winner == "O") return 3;
 			}
 		}
 		return $dotflag;
 	}
 
 	function check_cols($board) {
 		$board = indexshift($board);
 		$dotflag = 0;
 		foreach($board as $col) {
 			$status = 0;
 			$winner = -1;
 			foreach($col as $field) {
 				if($field == "T") $status++;
 				else if(in_array($field, array("O", "X"))){
 					if(in_array($winner, array(-1, $field))) {
 						$winner = $field;
 						$status++;
 					}
 				}
 				else if($field == ".") $dotflag = 1;
 			}
 			if($status == 4) {
 				if($winner == "X") return 2;
 				if($winner == "O") return 3;
 			}
 		}
 		return $dotflag;
 	}
 
 	function check_diag($board) {
 		$diags = array(array($board[0][0], $board[1][1], $board[2][2], $board[3][3]),
 			array($board[0][3], $board[1][2], $board[2][1], $board[3][0]));
 		$dotflag = 0;
 		foreach($diags as $diag) {
 			$status = 0;
 			$winner = -1;
 			foreach($diag as $field) {
 				if($field == "T") $status++;
 				else if(in_array($field, array("O", "X"))){
 					if(in_array($winner, array(-1, $field))) {
 						$winner = $field;
 						$status++;
 					}
 				}
 				else if($field == ".") $dotflag = 1;
 			}
 			if($status == 4) {
 				if($winner == "X") return 2;
 				if($winner == "O") return 3;
 			}
 		}
 		return $dotflag;
 	}
 
 	function indexshift($input) {
 		$return = array();
 		for($y = 0; $y < 4; $y++) {
 			for($x = 0; $x < 4; $x++) {
 				$return[$y][$x] = $input[$x][$y];
 			}
 		}
 		return $return;
 	}

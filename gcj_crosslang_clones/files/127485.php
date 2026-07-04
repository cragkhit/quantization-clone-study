<?php
 	$data = file_get_contents("A-large.in");
 	$lines = explode("\n", $data);
 	$board = array();
 	$arr = array();
 	$case = 1;
 
 	for($i = 0; $i < count($lines); $i++){
 		$line = $lines[$i];
 		if(is_numeric($line)){
 			// do nothing
 		} else if (empty($line)){
 			if(count($board) == 0){
 				// do nothing
 			} else {
 				$output = 'Case #' . $case++ . ': '; 
 
 				// print_r($board);
 
 				$X_wins = 0;
 				$O_wins = 0;
 
 				// For each row, check for O or X wins
 				foreach($board as $row){
 					if(in_array('.', $row) == false && in_array('O', $row) === false){
 						$X_wins++;
 					} else if(in_array('.', $row) === false && in_array('X', $row) === false){
 						$O_wins++;
 					}
 				}
 
 				// For each column, check for O or X wins
 				for($j=0; $j<count($row); $j++){
 					$_arr = array();
 					foreach($board as $row){
 						array_push($_arr, $row[$j]);
 					}
 					if(in_array('.', $_arr) === false && in_array('O', $_arr) === false){
 						$X_wins++;
 					}
 					if(in_array('.', $_arr) === false && in_array('X', $_arr) === false){
 						$O_wins++;
 					}
 				}
 
 				// Check \ diagonal
 				$_arr = array();
 				for($j=0; $j<count($row); $j++){
 					array_push($_arr, $board[$j][$j]);
 				}
 				if(in_array('.', $_arr) === false && in_array('O', $_arr) === false){
 					$X_wins++;
 				}
 				if(in_array('.', $_arr) === false && in_array('X', $_arr) === false){
 					$O_wins++;
 				}
 
 				// Check / diagonal
 				$_arr = array();
 				$k = 0;
 				for($j=count($row)-1; $j>=0; $j--){
 					array_push($_arr, $board[$k++][$j]);
 				}
 				if(in_array('.', $_arr) === false && in_array('O', $_arr) === false){
 					$X_wins++;
 				}
 				if(in_array('.', $_arr) === false && in_array('X', $_arr) === false){
 					$O_wins++;
 				}
 
 				if($X_wins > $O_wins){
 					$output .= "X won\n";
 				} else if ($O_wins > $X_wins) {
 					$output .= "O won\n";
 				} else {
 					// Make sure we're not still playing
 					// For each row, check for no-moves
 					$completed = true;
 					foreach($board as $row){
 						if(in_array('.', $row)){
 							$completed = false;
 							// $board = array();
 							// echo $output;
 
 							// continue 2;
 							break;
 						}
 					}
 					if($completed){
 						$output .= "Draw\n";
 					} else {
 						$output .= "Game has not completed\n"; 
 					}
 				}
 				echo $output;
 				$board = array();
 			}
 		} else {
 			// Add the line to our board
 			for($j=0; $j<strlen($line); $j++){
 				array_push($arr, $line[$j]);
 			}
 			array_push($board, $arr);
 			$arr = array();
 		}
 	}
 ?>
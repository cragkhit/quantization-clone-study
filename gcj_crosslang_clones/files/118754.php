<?php
 
 /***************************************************************************************************
 
  ** Google code jam ** 
 
  Author: 	Ismail Ozturk
  E-Mail: 	ismail.ozturk@gmail.com
  Problem:	Code Jam 2014 - Round 1B - Question A
 
 ***************************************************************************************************/
 
 $startTime = microtime(true);
 codejam::jam();
 echo "\r\n\r\n" . " Script executed in " . round(( microtime(true) - $startTime ),2) . " seconds.\r\n";
 
 
 class codejam
 {	
 	private static function deleteOutputFileIfExists() 
 	{
 		$outputFile = dirname(__FILE__) . '\\' . 'output.txt';
 		
 		if ( file_exists( $outputFile ) )
 		{
 			unlink( $outputFile );
 		}
 	}
 	
 	
 	private static function getInputFileHandle()
 	{
 		$inputFileHandle = dirname(__FILE__) . '\\' . 'input.txt';
 		return fopen( $inputFileHandle, 'r');
 	}
 	
 	
 	private static function closeFileHandle( $fileHandle )
 	{
 		return fclose( $fileHandle );
 	}
 	
 	
 	private static function writeToOutput( $lineToOutput )
 	{
 		$outputFile = dirname(__FILE__) . '\\' . 'output.txt';
 		return file_put_contents( $outputFile , $lineToOutput, FILE_APPEND );
 	}
 
 	
 	public static function jam()
 	{
 		self::deleteOutputFileIfExists();
 		$fh = self::getInputFileHandle();
 		
 		$fileLineCounter = 0;
 		$totalCaseCounter = 0;
 		$caseLineCounter = 0;
 		$case = 0;
 		
 		$numberOfStrings = 0;
 		$strings = array();
 		$moves = 0;
 		
 		while ( $line = trim( fgets( $fh ) ) )
 		{
 			$fileLineCounter++;
 			
 			if ( $fileLineCounter == 1 )
 			{
 				$totalCaseCounter = $line;
 				continue;
 			}
 			
 			$caseLineCounter++;
 			
 			if ( $caseLineCounter == 1 )
 			{
 				$numberOfStrings = $line;
 				continue;
 			}
 			
 			if ( $caseLineCounter <= $numberOfStrings + 1 )
 			{
 				$strings[] = $line;
 			}
 			
 			if ( $caseLineCounter == $numberOfStrings + 1 )
 			{
 				$s2 = array();
 				
 				for ( $i=0; $i<count($strings); $i++ )
 				{
 					$s2[$i]=array();
 					$letters = str_split($strings[$i]);
 					
 					$counter = 0;
 					
 					for ( $j=0; $j<count($letters); $j++)
 					{
 						if ( isset($letters[$j-1]) && $letters[$j-1] != $letters[$j] )
 						{
 							$counter++;
 						}
 						
 						if ( !isset($s2[$i][$letters[$j] . $counter]) )
 						{
 							$s2[$i][$letters[$j] . $counter] = 1;
 						}
 						else
 						{
 							$s2[$i][$letters[$j] .$counter ]++;
 						}
 						
 					}
 					
 				}
 				
 				//print_r($s2); exit();
 				
 				$sameFlag = true;
 				for ( $i=0; $i<count($s2); $i++ )
 				{
 					if ( isset($s2[$i-1]) and $s2[$i-1] != $s2[$i] )
 					{
 						$sameFlag = false;
 						break;
 					}
 				}
 				
 				if ( $sameFlag )
 				{
 					$moves = 0;
 				}
 				else
 				{
 					$feglaWinsFlag = false;
 					for ( $i=0; $i<count($s2); $i++ )
 					{
 						if ( isset($s2[$i-1]) )
 						{
 							$l1 = array_keys($s2[$i-1]);
 							$l2 = array_keys($s2[$i]);
 							
 							if ( count($l1) != count($l2) )
 							{
 								$feglaWinsFlag = true;
 								$moves = "Fegla Won";
 								break;
 							}
 							
 							//print_r($l1);
 							//print_r($l2);
 							
 							for( $j=0; $j<count($l1); $j++ )
 							{
 								if (  !isset($l1[$j]) or !isset($l2[$j]) or  $l1[$j] != $l2[$j] )
 								{
 									$feglaWinsFlag = true;
 									$moves = "Fegla Won";
 									break;
 								}
 							}
 						}
 						
 						if ( $feglaWinsFlag )
 						{
 							break;
 						}
 					}
 					
 					if ( !$feglaWinsFlag )
 					{
 						//what is the min number of moves?
 						$moves = 0;
 						
 						foreach ( $s2[0] as $k=>$v )
 						{
 							$m = array();
 							
 							for ( $i=0; $i<count($s2); $i++ )
 							{
 								//echo $i . ' ' . $k . ' ' . $s2[$i][$k] . "\r\n";
 								$m[] = $s2[$i][$k];
 							}
 							
 							//number of moves is biggest - smallest
 							$max = max( $m );
 							$min = min( $m );
 							
 							//echo $k . " " . $max . " " . $min . "\r\n";
 							
 							$moves += ( $max - $min );
 							
 						}
 						
 					}
 					
 				}
 			
 				$case++;
 				$output = "Case #" . $case .": " . $moves;
 
 				if	( $case < $totalCaseCounter )	$output .= "\r\n";
 
 				echo ' ' . $output;
 				self::writeToOutput( $output );
 
 				$caseLineCounter = 0;
 				
 				$numberOfStrings = 0;
 				$strings = array();
 				$moves = 0;
 			}			
 			
 		}
 	}
 	
 }
 
 
 ?>
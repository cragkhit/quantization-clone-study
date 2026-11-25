<?php
 
 class RoundOneController extends \BaseController {
 
 	/**
 	 * Display a listing of the resource.
 	 * GET /roundone
 	 *
 	 * @return Response
 	 */
 	public function index()
 	{
         ini_set('max_execution_time', 300);
         ini_set('memory_limit', '2048M');
         $file = File::get('A-large.in');
         $file_array = preg_split ('/$\R?^/m', $file);
         $T = '';
         $output = '';
         $i = 0;
         foreach ($file_array as $line) {
             if ($i == 0) {
                 $T = $line;
             } else {
 
                 $result = array();
                 $line_split = explode(' ', $line);
                 $audience_total = intval($line_split[0]);
                 $audience_individuals = str_split($line_split[1]);
                 $standing_now = 0;
                 $invites_total = 0;
                 for ($j=0; $j <= $audience_total; $j++){
                     if ($j < 1) {
                         $standing_now = $audience_individuals[$j];
                     } else {
                         $required = $j > $standing_now ? $j - $standing_now : 0;
                         $invites_total += $required;
                         $standing_now += $audience_individuals[$j] + $required;
                     }
                 }
                 $output .= "Case #$i: $invites_total<br>";
             }
             $i++;
         }
         echo "$output";
         return 1;
 	}
 
 	/**
 	 * Show the form for creating a new resource.
 	 * GET /roundone/create
 	 *
 	 * @return Response
 	 */
 	public function create()
 	{
 		//
 	}
 
 	/**
 	 * Store a newly created resource in storage.
 	 * POST /roundone
 	 *
 	 * @return Response
 	 */
 	public function store()
 	{
 		//
 	}
 
 	/**
 	 * Display the specified resource.
 	 * GET /roundone/{id}
 	 *
 	 * @param  int  $id
 	 * @return Response
 	 */
 	public function show($id)
 	{
 		//
 	}
 
 	/**
 	 * Show the form for editing the specified resource.
 	 * GET /roundone/{id}/edit
 	 *
 	 * @param  int  $id
 	 * @return Response
 	 */
 	public function edit($id)
 	{
 		//
 	}
 
 	/**
 	 * Update the specified resource in storage.
 	 * PUT /roundone/{id}
 	 *
 	 * @param  int  $id
 	 * @return Response
 	 */
 	public function update($id)
 	{
 		//
 	}
 
 	/**
 	 * Remove the specified resource from storage.
 	 * DELETE /roundone/{id}
 	 *
 	 * @param  int  $id
 	 * @return Response
 	 */
 	public function destroy($id)
 	{
 		//
 	}
 
 }
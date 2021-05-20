<?
// REQUEST: GET
// Query {latitude_t: ?float, longitude_l: ?float, latitude_b: ?float, longitude_r: ?float, dogs: ?int, dog_parks: ?int, dog_friendly: ?int, animal_hospitals: ?int, shelters: ?int, pet_stores: ?int, dog_hotels: ?int, training_clubs: ?int, breeding: ?int, male: ?int, female: ?int, birthday: ?string(from : to), castration: ?int, ready_to_mate: ?int, for_sale: ?int, breeds: ?string(n1|n2|...)} 
//
// RETURN:
// json [{"id": int, "latitude": double, "longitude": double}, ...]
//
// ERRORS:
// 500:
// server error;

defined('APP_RAN') or die();

// getting query
$data = $_GET;

// + north, - south
$latitude_t = 90;
$latitude_b = -90;

// + east, - west
$longitude_l = -180;
$longitude_r = 180;

// check query
if(isset($data["latitude_t"]))
	$latitude_t = $data["latitude_t"];


// check query
if(isset($data["latitude_b"]))
	$latitude_b = $data["latitude_b"];
	

// check query
if(isset($data["longitude_l"]))
	$longitude_l = $data["longitude_l"];


// check query
if(isset($data["longitude_r"]))
	$longitude_r = $data["longitude_r"];

// is longitude_l > longitude_r
$reverse_long = false;

if($longitude_r < $longitude_l)
	$reverse_long = true;

// find markers
$markers = [];

// ----------------------------------- DOGS ------------------------------------------------------------------------------------------
if(isset($data["dogs"]) && $data["dogs"] == 1){
	$arr = [];
	
	// dog special filters
	$special_filetrs = ["is_set_location = 1"];
	
	// preparing db request
	
	$dbReq = "SELECT id, latitude, longitude FROM dogs WHERE ";
	
	if(!$reverse_long)
		$special_filetrs[] = "latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$special_filetrs[] = "latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";
	
	$bind_par1 = "dddd";
	$bind_par2 = [$latitude_t, $latitude_b, $longitude_l, $longitude_r];
	
	// sex filters
	if(isset($data["male"]) && $data["male"] == 1 && (!isset($data["female"]) || isset($data["female"]) && $data["female"] == 0))
		$special_filetrs[] = "sex = 0";
	if(isset($data["female"]) && $data["female"] == 1 && (!isset($data["male"]) || isset($data["male"]) && $data["male"] == 0))
		$special_filetrs[] = "sex = 1";
	
	// birthday filters
	if(isset($data["birthday"]) && !empty($data["birthday"])){
		$dates = explode(" : ", $data["birthday"]);
		$special_filetrs[] = "birthday >= ? AND birthday <= ?";
		
		$bind_par1 .= "ss";
		$bind_par2[] = $dates[0];
		$bind_par2[] = $dates[1];
	}
	
	// castration filters
	if(isset($data["castration"]) && $data["castration"] == 1)
		$special_filetrs[] = "castration = 1";
	
	// ready_to_mate filters
	if(isset($data["ready_to_mate"]) && $data["ready_to_mate"] == 1)
		$special_filetrs[] = "ready_to_mate = 1";
	
	// for_sale filters
	if(isset($data["for_sale"]) && $data["for_sale"] == 1)
		$special_filetrs[] = "for_sale = 1";
	
	// breed filters
	if(isset($data["breeds"]) && !empty($data["breeds"])){
		$breeds = explode("|", $data["breeds"]);
		$breed_filters = [];
		foreach($breeds as &$value){
			$bind_par1 .= "s";
			$bind_par2[] = $value;
			$breed_filters[] = "breed = ?";
		}
		$special_filetrs[] = "(" . implode(" OR ", $breed_filters) . ")";
	}
	
	$dbReq .= implode(" AND ", $special_filetrs);
	

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, $bind_par1, ...$bind_par2);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"0");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- DOG_PARKS ------------------------------------------------------------------------------------------
if(isset($data["dog_parks"]) && $data["dog_parks"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM dog_parks WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM dog_parks WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"1");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- DOG_FRIENDLY ------------------------------------------------------------------------------------------
if(isset($data["dog_friendly"]) && $data["dog_friendly"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM dog_friendly WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM dog_friendly WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"2");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- ANIMAL_HOSPITALS ------------------------------------------------------------------------------------------
if(isset($data["animal_hospitals"]) && $data["animal_hospitals"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM animal_hospitals WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM animal_hospitals WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"3");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- SHELTERS ------------------------------------------------------------------------------------------
if(isset($data["shelters"]) && $data["shelters"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM shelters WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM shelters WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt, $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"4");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- PET_STORES ------------------------------------------------------------------------------------------
if(isset($data["pet_stores"]) && $data["pet_stores"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM pet_stores WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM pet_stores WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"5");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- DOG_HOTELS ------------------------------------------------------------------------------------------
if(isset($data["dog_hotels"]) && $data["dog_hotels"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM dog_hotels WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM dog_hotels WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"6");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- TRAINING_CLUBS ------------------------------------------------------------------------------------------
if(isset($data["training_clubs"]) && $data["training_clubs"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM training_clubs WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM training_clubs WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"7");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- BREEDING ------------------------------------------------------------------------------------------
if(isset($data["breeding"]) && $data["breeding"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM breeding WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM breeding WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"8");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}

// ----------------------------------- DANGER ------------------------------------------------------------------------------------------
/*if(isset($data["dogs"]) && $data["dogs"] == 1){
	$arr = [];
	// preparing db request
	
	$dbReq;
	if(!$reverse_long)
		$dbReq = "SELECT id, latitude, longitude FROM dogs WHERE latitude <= ? AND latitude >= ? AND longitude >= ? AND longitude <= ?";
	else
		$dbReq = "SELECT id, latitude, longitude FROM dogs WHERE latitude <= ? AND latitude >= ? AND (longitude >= ? OR longitude <= ?)";

	$stmt = mysqli_prepare($link, $dbReq);
	
	mysqli_stmt_bind_param($stmt, 'dddd', $latitude_t, $latitude_b, $longitude_l, $longitude_r);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $bd_id, $lat, $long);


	// getting
	while(mysqli_stmt_fetch ($stmt)){
		$arr[] = array("id"=>$bd_id, "latitude"=>$lat, "longitude"=> $long, "markerType"=>"9");
	}	

	// closing statement
	mysqli_stmt_close ($stmt);
	
	$markers = array_merge($markers, $arr);
}*/

// response json
echo json_encode($markers);

?>
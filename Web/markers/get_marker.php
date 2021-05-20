<?
// REQUEST: GET
// Query {id: int, type: int}
//
// RETURN:
// json 
//
// ERRORS:
// 400:
// np type given
//
// 500:
// server error;

defined('APP_RAN') or die();
require("../general_files/passwords.php");

// getting query
$data = $_GET;

// check query
if(!isset($data["type"])){
	http_response_code(400);
	exit();
}

// ------------------------------------ DOG ----------------------------------------
if($data["type"] == 0){
	
	// preparing db request
	$stmt = mysqli_prepare($link, "SELECT user_id, name, breed, birthday, sex, image FROM dogs WHERE id = ?");
	
	mysqli_stmt_bind_param($stmt, 'd', $data["id"]);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt, $user_id, $name, $breed, $birthday, $sex, $image);

	// getting
	mysqli_stmt_fetch ($stmt);

	// closing statement
	mysqli_stmt_close ($stmt);
	
	
	// getting owner user name
	// preparing db request
	$stmt = mysqli_prepare($link, "SELECT name FROM users WHERE id = ?");
	
	mysqli_stmt_bind_param($stmt, 'd', $user_id);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $user_name);

	// getting
	mysqli_stmt_fetch ($stmt);

	// closing statement
	mysqli_stmt_close ($stmt);


	
	// response json
	echo json_encode(["json"=>json_encode(["id"=>$data["id"], "user_id"=>$user_id, "name"=>$name, "breed"=>$breed, "birthday"=>$birthday, "sex"=>$sex, "image"=>$base_url . "/" . $image, "user_name"=>$user_name])]); 
}
//------------------------------------------------------------------------- DOG_PARK -----------------------------
else if($data["type"] == 1){		
	// preparing db request
	$stmt = mysqli_prepare($link, "SELECT latitude, longitude, area, lighting, fencing, elements, working_hours, image FROM dog_parks WHERE id = ?");
	
	mysqli_stmt_bind_param($stmt, 'd', $data["id"]);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt, $latitude, $longitude, $area, $lighting, $fencing, $elements, $working_hours, $image);

	// getting
	mysqli_stmt_fetch ($stmt);

	// closing statement
	mysqli_stmt_close ($stmt);
	
	// response json
	echo json_encode(["json"=>json_encode(["latitude"=>$latitude, "longitude"=>$longitude, "id"=>$data["id"], "area"=>$area, "lighting"=>$lighting, "fencing"=>$fencing, "elements"=>$elements, "image"=>$base_url . "/" . $image, "working_hours"=>$working_hours])]); 
}
//------------------------------------------------------------------------- PLACES -----------------------------
else{
	$types = ["dog_friendly", "animal_hospitals", "shelters", "pet_stores", "dog_hotels", "training_clubs", "breeding"];
	$req = "SELECT latitude, longitude, name, contacts, website, details, working_hours, image FROM " . $types[$data["type"] - 2] . " WHERE id = ?";
	
		
	// preparing db request
	$stmt = mysqli_prepare($link, $req);
	
	mysqli_stmt_bind_param($stmt, 'd', $data["id"]);
	
	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}
	
	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt, $latitude, $longitude, $name, $contacts, $website, $details, $working_hours, $image);

	// getting
	mysqli_stmt_fetch ($stmt);

	// closing statement
	mysqli_stmt_close ($stmt);
	
	// response json
	echo json_encode(["json"=>json_encode(["latitude"=>$latitude, "longitude"=>$longitude, "id"=>$data["id"], "name"=>$name, "contacts"=>$contacts, "website"=>$website, "details"=>$details, "image"=>$base_url . "/" . $image, "working_hours"=>$working_hours])]); 
}
?>
<?
// REQUEST: POST
// Headers {jwt:string}
// Body json {"name": string, "sex": int(0 - male, 1 - female), "birtday": string(yyyy-mm-dd), "breed": string, "castration": bool, "ready_to_mate": bool, "for_sale": bool}
// File "image"
//
// ERRORS:
// 401:
// no jwt token given; 
// incorrect token;
//
// 422:
// Incorrect date format;
//
// 500:
// server error;

require("../general_files/check_jwt.php");
require("../general_files/generate_random_string.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

$data = json_decode($_POST["dog"], true);

// check body
if(!isset($data["name"]) || !isset($data["sex"]) || !isset($data["birthday"]) || !isset($data["breed"]) || !isset($data["castration"]) || !isset($data["ready_to_mate"]) || !isset($data["for_sale"])){
	http_response_code(400);
	exit();
}

if (!DateTime::createFromFormat('Y-m-d', $data["birthday"])) {
	http_response_code(422);
	exit();
}

$image = "";

if($_FILES["image"]["size"] > 0){
	$uploaddir = '../images/dogs/';
	$path_parts = pathinfo($_FILES["image"]["name"]);

	if ($_FILES["image"]["type"] == "image/*"){
		$image = $uploaddir . generateRandomString($length = 20) . "." . $path_parts['extension'];
			
	   if ($_FILES["image"]["error"] > 0){
			echo "Return Code: " . $_FILES["image"]["error"];
		   	http_response_code(500);
			exit();
	   }
	   else{
		   move_uploaded_file($_FILES["image"]["tmp_name"], $image);
	   }
	}
}

// define location
// get all dogs
// preparing db request
$stmt = mysqli_prepare($link, "SELECT id FROM dogs WHERE user_id = ?");
mysqli_stmt_bind_param($stmt, 's', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $dog_id);

$arr = [];
// getting
while(mysqli_stmt_fetch ($stmt)){
	$arr[] = $dog_id;
}	

// closing statement
mysqli_stmt_close ($stmt);

// get user location
// preparing db request
$stmt = mysqli_prepare($link, "SELECT latitude, longitude, is_set_location FROM users WHERE id = ?");
mysqli_stmt_bind_param($stmt, 's', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $latitude, $longitude, $is_set_location);

$arr = [];
// getting
while(mysqli_stmt_fetch ($stmt)){
	$arr[] = $dog_id;
}	

// closing statement
mysqli_stmt_close ($stmt);

$latitude = $latitude + 0.00001 * count($arr);


// add dog to database
// preparing db request
$stmt = mysqli_prepare($link, "INSERT INTO dogs SET user_id = ?, name = ?, sex = ?, birthday = ?, breed = ?, castration = ?, ready_to_mate = ?, for_sale = ?, image = ?, is_set_location = ?, latitude = ?, longitude = ?");
mysqli_stmt_bind_param($stmt, 'dsdssdddsddd', $jwt_payload["user_id"], $data["name"], $data["sex"], $data["birthday"], $data["breed"], $data["castration"], $data["ready_to_mate"], $data["for_sale"], $image, $is_set_location, $latitude, $longitude);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// created user's id	
$new_dog_id = mysqli_insert_id($link);
	
// closing statement
mysqli_stmt_close ($stmt);

echo(json_encode(["dog_id"=>$new_dog_id]));
?>

<?
// REQUEST: PUT
// Headers {jwt: string}
// Query {id: int}
// Body json {"name": string, "sex": int(0 - male, 1 - female), "birtday": string(yyyy-mm-dd), "breed": string, "castration": bool, "ready_to_mate": bool, "for_sale": bool}
// File "image"
//
// ERRORS:
// 400:
// no id given;
//
// 401: 
// no jwt token given; 
// incorrect token;
//
// 403:
// not an owner;
//
// 404:
// dog not found;
//
// 422:
// wrong date format;
//
// 500:
// server error;

require("../general_files/config.php");
require("../general_files/generate_random_string.php");
require("../general_files/check_jwt.php");

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

$data = json_decode($_POST["dog"], true);

// getting query
if(!isset($_GET["id"])){
	echo "no id given";
	http_response_code(400);
	exit();
}

$dog_id = $_GET["id"];


// get dog
// preparing db request
$stmt = mysqli_prepare($link, "SELECT user_id, name, sex, birthday, breed, castration, ready_to_mate, for_sale, image FROM dogs WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $dog_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

mysqli_stmt_store_result($stmt);

// check if dog exists
if(mysqli_stmt_num_rows($stmt) != 1){
	echo "dog not found";
	http_response_code(404);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt, $user_id, $name, $sex, $birthday, $breed, $castration, $ready_to_mate, $for_sale, $image);

// getting dog
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);


// check if user is owner
if($user_id != $jwt_payload["user_id"]){
	http_response_code(403);
	exit();
}


// change info
if(isset($data["name"])){
	$name = $data["name"];
}

if(isset($data["sex"])){
	$sex = $data["sex"];
}

if(isset($data["birthday"])){
	if (!DateTime::createFromFormat('Y-m-d', $data["birthday"])) {
		http_response_code(422);
		exit();
	}
	
	$birthday = $data["birthday"];
}

if(isset($data["breed"])){
	$breed = $data["breed"];
}

if(isset($data["castration"])){
	$castration = $data["castration"];
}

if(isset($data["ready_to_mate"])){
	$ready_to_mate = $data["ready_to_mate"];
}

if(isset($data["for_sale"])){
	$for_sale = $data["for_sale"];
}

// work with image
// ----------------------------------------------------------------------------------------------
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

if(isset($data["delete_photo"]) && $data["delete_photo"] == 1){
	$image = "";
}

// ToDo remove previous image ----------------------------------------------------------------------------


// set dog
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE dogs SET name = ?, sex = ?, birthday = ?, breed = ?, castration = ?, ready_to_mate = ?, for_sale = ?, image = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sdssdddsd', $name, $sex, $birthday, $breed, $castration, $ready_to_mate, $for_sale, $image, $dog_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);
?>
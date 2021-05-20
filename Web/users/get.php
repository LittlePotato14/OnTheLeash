<?
// REQUEST: GET
// Headers {jwt: string?}
// Query {id: int?}
//
// RETURN:
// json {
// "id": int,
// "name": string,
// "username": string,
// "description": string,
// "avatar": string
// "dogs":[ {"id": int, "name": string, "sex": int(0 - male, 1 - female), "birtday": string(yyyy-mm-dd), "breed": string, "castration": bool, "ready_to_mate": bool, "for_sale": bool, "image": string}, ... ]
// }
//
// ERRORS:
// 401:
// no jwt token given; 
// incorrect token;
//
// 404: 
// user not found;
//
// 500:
// server error;

require("../general_files/check_jwt.php");
require("../general_files/passwords.php");

defined('APP_RAN') or die();

// getting query
$data = $_GET;

$search_id = 0;

// check body
if(isset($data["id"])){
	$search_id = $data["id"];
}
else{
	// check jwt
	$jwt_check = checkJwt($link);
	$jwt_payload = $jwt_check["jwt_payload"];
	$search_id = $jwt_payload["user_id"];
}

// get user from db
// preparing db request
$stmt = mysqli_prepare($link, "SELECT name, username, description, avatar, latitude, longitude, is_set_location FROM users WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $search_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

mysqli_stmt_store_result($stmt);

// check if id exists
if(mysqli_stmt_num_rows($stmt) != 1){
	echo "user not found";
	http_response_code(404);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $name, $username, $description, $avatar, $latitude, $longitude, $is_location_set);

// getting user info
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);


// get dogs from db
$dogs = [];
// preparing db request
$stmt = mysqli_prepare($link, "SELECT id, name, sex, birthday, breed, castration, ready_to_mate, for_sale, image FROM dogs WHERE user_id = ?");
mysqli_stmt_bind_param($stmt, 'd', $search_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt, $dog_id, $dog_name, $sex, $birthday, $breed, $castration, $ready_to_mate, $for_sale, $image);

// getting dog info
while(mysqli_stmt_fetch ($stmt)){
	$dogs[] = array("id"=>$dog_id, "name" => $dog_name, "sex" => $sex, "birthday" => $birthday, "breed" => $breed, "castration" => $castration, "ready_to_mate" => $ready_to_mate, "for_sale" => $for_sale, "image"=>$base_url . "/" . $image);
}	

// closing statement
mysqli_stmt_close ($stmt);

// response json
echo json_encode(["id"=>$search_id, "name"=>$name, "username"=>$username, "description"=>$description, "latitude"=>$latitude, "longitude"=>$longitude, "is_location_set"=>$is_location_set, "avatar"=> $base_url . "/" . $avatar, "dogs"=>$dogs]);
?>
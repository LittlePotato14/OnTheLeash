<?
// REQUEST: PUT
// Headers {jwt: string}
// Body json {latitude: float, longitude: float}
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token;
//
// 422:
// wrong coordinates format
//
// 500:
// server error;

require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// check corrdinates
if (!is_float($data["latitude"]) || !is_float($data["longitude"]) 
	|| $data["latitude"] > 90 ||$data["latitude"] < -90
	|| $data["longitude"] > 180 || $data["longitude"] < -180) {
	echo "Wrong corrdinates format";
	http_response_code(422);
	exit();
}


$email = $data["email"];

// set user
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET latitude = ?, longitude = ?, is_set_location = 1 WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'ddd', $data["latitude"], $data["longitude"], $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);

// get dogs
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

$cur_latitude = $data["latitude"];

for($i = 0; $i < count($arr); $i++){
	// set dog
	// preparing db request
	$stmt = mysqli_prepare($link, "UPDATE dogs SET latitude = ?, longitude = ?, is_set_location = 1 WHERE id = ?");
	mysqli_stmt_bind_param($stmt, 'ddd', $cur_latitude, $data["longitude"], $arr[$i]);

	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}

	// closing statement
	mysqli_stmt_close ($stmt);
	
	if(!($cur_latitude + 0.00001 > 90))
		$cur_latitude += 0.00001;
}
?>
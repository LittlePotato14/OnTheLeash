<?
// REQUEST: PUT
// Headers {jwt: string}
// Body json {confirmation_code: string}
//
// RETURN:
// json {jwt: string]);
//
// ERRORS:
// 400:
// no confirmation code given;
//
// 401: 
// no jwt token given; 
// incorrect token; 
// wrong confirmation code;
//
// 410:
// out of attempts for current code
//
// 500:
// server error;

require("../general_files/check_jwt.php");
require("../general_files/generate_random_string.php");
require("../general_files/generate_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link, false);
$jwt_payload = $jwt_check["jwt_payload"];

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// check body
if(!isset($data["confirmation_code"])){
	echo "No confirmation code given";
	http_response_code(400);
	exit();
}

// check code
// preparing db request
$stmt = mysqli_prepare($link, "SELECT code, attempts FROM confirmation_codes WHERE user_id = ?");
mysqli_stmt_bind_param($stmt, 's', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $code_from_db, $code_attempts);

// getting db saved code
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

$code_attempts = $code_attempts - 1;
// remove code from db if it has 0 attempts
if($code_attempts == 0){
	// preparing db request
	$stmt = mysqli_prepare($link, "DELETE FROM confirmation_codes WHERE user_id = ?");
	mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}

	// closing statement
	mysqli_stmt_close ($stmt);
}
// else update code attempts
else{
	// preparing db request
	$stmt = mysqli_prepare($link, "UPDATE confirmation_codes SET attempts = ? WHERE user_id = ?");
	mysqli_stmt_bind_param($stmt, 'dd', $code_attempts, $jwt_payload["user_id"]);

	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}

	// closing statement
	mysqli_stmt_close ($stmt);
}

// check codes match
if($data["confirmation_code"] != $code_from_db){
	echo "Wrong confirmation code";
	if($code_attempts == 0){
		http_response_code(410);
	}
	else{
		http_response_code(401);
	}
	exit();
}

// activate account
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET confirmed = 1 WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);

// generating secret key
$secret_key = generateRandomString();
// generating jwt token
$jwt = generateJwt($jwt_payload["user_id"], $secret_key);

// add secret key to db
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET users.keys = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $secret_key, $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);

// return active jwt token
echo json_encode(["jwt"=>$jwt]);
?>

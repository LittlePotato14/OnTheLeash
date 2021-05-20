<?
// REQUEST: PUT
// Headers {jwt: string}
// Body json {password: string}
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token;
//
// 422:
// wrong  password format;
// password match;
//
// 500:
// server error;

require("../general_files/generate_random_string.php");
require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// spliting password parts
$parts = explode(".", $password);
$salt = $parts[0];
$hash_password = $parts[1];

if(md5($salt . "." .  $data["password"]) == $hash_password){
	echo "Password match";
	http_response_code(422);
	exit();
}

$password_pattern = "/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/";

// check for correct password
if(!preg_match ($password_pattern, $data["password"])){
	echo "Wrong password format";
	http_response_code(422);
	exit();
}

// generate salt
$salt = generateRandomString($length = rand(6, 10));

$password = $salt . "." . md5($salt . "." .  $data["password"]);


// logout others 
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET users.keys = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $jwt_check["key_found"], $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);


// set user
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET password = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $password, $jwt_payload["user_id"]);

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

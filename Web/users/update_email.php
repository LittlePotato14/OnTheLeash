<?
// REQUEST: PUT
// Headers {jwt: string}
// Body json {email: string}
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token;
//
// 422:
// wrong email format;
// email address already exists;
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

// check email
$email_pattern = "/^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/";
// check for correct email
if (!preg_match($email_pattern, $data["email"])) {
	echo "Wrong email format";
	http_response_code(422);
	exit();
}

// preparing db request
$stmt = mysqli_prepare($link, "SELECT id, email FROM users");

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt, $db_id, $db_email);

// check for matching emails
while (mysqli_stmt_fetch ($stmt)) {
	if($db_email == $data["email"] && $db_id != $jwt_payload["user_id"]){
		echo "This email address already exists";
		http_response_code(422);
		exit();
	}
}

// closing statement
mysqli_stmt_close ($stmt);

$email = $data["email"];


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
$stmt = mysqli_prepare($link, "UPDATE users SET email = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $data["email"], $jwt_payload["user_id"]);

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
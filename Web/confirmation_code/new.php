<?
// REQUEST: PUT
// Headers {jwt: string}
//
// RETURN:
// ---
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token; 
//
// 500:
// server error;

require("../general_files/check_jwt.php");
require("../general_files/generate_random_string.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link, false);
$jwt_payload = $jwt_check["jwt_payload"];

// delete old code from db
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

// add new code to db
// generate code
$email_code = generateRandomDigitString();

// preparing db request
$stmt = mysqli_prepare($link, "INSERT INTO confirmation_codes SET user_id = ?, code = ?");
mysqli_stmt_bind_param($stmt, 'ds', $jwt_payload["user_id"], $email_code);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}
	
// closing statement
mysqli_stmt_close ($stmt);

// get email
// preparing db request
$stmt = mysqli_prepare($link, "SELECT email FROM users WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $db_email);

// getting db saved password
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

// send request
ignore_user_abort(true);
ob_start();
http_response_code(201);
header('Connection: close');
header('Content-Length: '.ob_get_length());
ob_end_flush();
ob_flush();
flush();

require("../general_files/send_email.php");
// send email
sendEmail($db_email, 'Подтверждение регистрации na-povodke', 'Новый код подтверждения: ' . $email_code);
?>
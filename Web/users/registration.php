<?
// REQUEST: POST
// Body json {email: string, password: string}
//
// RETURN:
// json {jwt: string]);
//
// ERRORS:
// 400:
// no email or password given;
//
// 422:
// Incorrect email format;
// Incorrect password format;
// Email address already exists;
//
// 500:
// server error;

require("../general_files/generate_random_string.php");
require("../general_files/generate_jwt.php");

defined('APP_RAN') or die();

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// check body
if(!isset($data["email"]) || !isset($data["password"])){
	http_response_code(400);
	exit();
}

$email_pattern = "/^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/";

// check for correct email
if (!preg_match($email_pattern, $data["email"])) {
	echo "Wrong email format";
    http_response_code(422);
	exit();
}

// preparing db request
$stmt = mysqli_prepare($link, "SELECT email FROM users");

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $db_email);

// check for matching emails
while (mysqli_stmt_fetch ($stmt)) {
	if($db_email == $data["email"]){
		echo "This email address already exists";
		http_response_code(422);
		exit();
	}
}
		
// closing statement
mysqli_stmt_close ($stmt);

$password_pattern = "/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/";

// check for correct password
if(!preg_match ($password_pattern, $data["password"])){
	echo "Wrong password format";
    http_response_code(422);
	exit();
}

// generate code
$email_code = generateRandomDigitString();

// generate salt
$salt = generateRandomString($length = rand(6, 10));

// generate secret key
$secret_key = generateRandomString();

// add user to database
// preparing db request
$stmt = mysqli_prepare($link, "INSERT INTO users SET email = ?, password = ?, users.keys = ?");
$new_password = $salt . "." . md5($salt . "." .  $data["password"]);
mysqli_stmt_bind_param($stmt, 'sss', $data["email"], $new_password, $secret_key);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// created user's id	
$new_user_id = mysqli_insert_id($link);
	
// closing statement
mysqli_stmt_close ($stmt);

// udate username and name
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET username = ?, name = ? WHERE id = ?");
$new_username = "user" . $new_user_id;
mysqli_stmt_bind_param($stmt, 'ssd', $new_username, $new_username, $new_user_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}
	
// closing statement
mysqli_stmt_close ($stmt);

// add email code to db
// preparing db request
$stmt = mysqli_prepare($link, "INSERT INTO confirmation_codes SET user_id = ?, code = ?");
mysqli_stmt_bind_param($stmt, 'ds', $new_user_id, $email_code);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}
	
// closing statement
mysqli_stmt_close ($stmt);

// create unactive jwt
$unactive_jwt = generateJwt($new_user_id, $secret_key, false);

// send unactive token to user
ignore_user_abort(true);
ob_start();
http_response_code(201);
echo json_encode(["jwt"=>$unactive_jwt]);
header('Connection: close');
header('Content-Length: '.ob_get_length());
ob_end_flush();
ob_flush();
flush();

require("../general_files/send_email.php");
// send email
sendEmail($data["email"], 'Подтверждение регистрации na-povodke', 'Код подтверждения: ' . $email_code);
?>

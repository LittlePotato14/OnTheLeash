<?
// REQUEST: PUT
// Headers {jwt: string}
// Body json {email: string, password: string, name: string, username: string, description: string}
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token;
//
// 422:
// wrong email format;
// email address already exists;
// wrong  password format;
// username is taken;
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

// get user
// preparing db request
$stmt = mysqli_prepare($link, "SELECT email, password, username, name, description FROM users WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt, $email, $password, $username, $name, $description);

// getting user
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

$email_changed = false;
// check if email is valid
if(isset($data["email"]) && $data["email"] != $email){
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
	$email_changed = true;
}


// check if username is valid
if(isset($data["username"])){	
	// preparing db request
	$stmt = mysqli_prepare($link, "SELECT id, username FROM users");

	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}

	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt, $db_id, $db_username);

	// check for matching emails
	while (mysqli_stmt_fetch ($stmt)) {
		if($db_username == $data["username"] && $db_id != $jwt_payload["user_id"]){
			echo "This username is taken";
			http_response_code(422);
			exit();
		}
	}
	
	// closing statement
	mysqli_stmt_close ($stmt);
		
	$username = $data["username"];
}


// check if password is valid
$password_changed = false;

// spliting password parts
$parts = explode(".", $password);
$salt = $parts[0];
$hash_password = $parts[1];

if(isset($data["password"]) && md5($salt . "." .  $data["password"]) != $hash_password){
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
	$password_changed = true;
}


if(isset($data["name"])){
	$name = $data["name"];
}


if(isset($data["description"])){
	$description = $data["description"];
}


// logout others if email or password changed
if($email_changed || $password_changed){
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
}


// set user
// preparing db request
	$stmt = mysqli_prepare($link, "UPDATE users SET email = ?, password = ?, username = ?, name = ?, description = ? WHERE id = ?");
	mysqli_stmt_bind_param($stmt, 'sssssd', $email, $password, $username, $name, $description, $jwt_payload["user_id"]);

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

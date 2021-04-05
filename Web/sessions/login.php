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
// 401: 
// wrong email or password;
// not confirmed user;
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

// preparing db request
$stmt = mysqli_prepare($link, "SELECT password, id, `users`.keys, confirmed FROM users WHERE email = ?");
mysqli_stmt_bind_param($stmt, 's', $data["email"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

mysqli_stmt_store_result($stmt);

// check if email exists
if(mysqli_stmt_num_rows($stmt) != 1){
	http_response_code(401);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $password, $user_id, $keys, $confirmed);

// getting db saved password
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

if(!$confirmed){
	echo "Not confirmed user";
	http_response_code(401);
	exit();
}

// spliting password parts
$parts = explode(".", $password);
$salt = $parts[0];
$hash_password = $parts[1];

// checking for correct password
if(md5($salt . "." .  $data["password"]) != $hash_password){
	http_response_code(401);
	exit();
}

// generating secret key
$secret_key = generateRandomString();
// generating jwt token
$jwt = generateJwt($user_id, $secret_key);
// add secret key to db
if(!empty($keys))
	$keys .= ",";
$keys .= $secret_key;

// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET users.keys = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $keys, $user_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);

// return jwt token
echo json_encode(["jwt"=>$jwt]);
?>

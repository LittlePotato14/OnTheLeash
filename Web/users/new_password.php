<?
// REQUEST: DELETE
// Body json {email: string}
//
// RETURN:
// ---
//
// ERRORS:
// 400:
// no email given
//
// 401:
// email does not exists
//
// 500:
// server error;

require("../general_files/config.php");
require("../general_files/send_email.php");
require("../general_files/generate_random_string.php");

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// check body
if(!isset($data["email"])){
	echo "no email given";
	http_response_code(400);
	exit();
}

// get user
// preparing db request
$stmt = mysqli_prepare($link, "SELECT id FROM users WHERE email = ?");
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
mysqli_stmt_bind_result ($stmt , $user_id);

// getting db saved id
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

// generate password
$password = generateRandomPassword();

// generate salt
$salt = generateRandomString($length = rand(6, 10));

// set password to user
$db_password = $salt . "." . md5($salt . "." .  $password);

// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET password = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', $db_password, $user_id);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}
	
// closing statement
mysqli_stmt_close ($stmt);

// send password in email
sendEmail($data["email"], 'Восстановление пароля na-povodke', 'Ваш новый временный пароль: ' . $password . "\nВы можете поменять пароль в личном кабинете после авторизации.");

require("../general_files/end_config.php");
?>
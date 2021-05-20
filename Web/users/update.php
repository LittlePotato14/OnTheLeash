<?
// REQUEST: POST
// Headers {jwt: string}
// Body json {name: string, username: string, description: string}
// File "image"
//
// ERRORS:
// 401: 
// no jwt token given; 
// incorrect token;
//
// 422:
// username is taken;
//
// 500:
// server error;

require("../general_files/config.php");
require("../general_files/generate_random_string.php");
require("../general_files/check_jwt.php");

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

$data = json_decode($_POST["user"], true);

// get user
// preparing db request
$stmt = mysqli_prepare($link, "SELECT username, name, description, avatar FROM users WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt, $username, $name, $description, $avatar);

// getting user
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

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

if(isset($data["name"])){
	$name = $data["name"];
}


if(isset($data["description"])){
	$description = $data["description"];
}

// ----------------------------------------------------------------------------------------------
if($_FILES["image"]["size"] > 0){
	$uploaddir = '../images/users/';
	$path_parts = pathinfo($_FILES["image"]["name"]);

	if ($_FILES["image"]["type"] == "image/*"){
		$avatar = $uploaddir . generateRandomString($length = 20) . "." . $path_parts['extension'];
			
	   if ($_FILES["image"]["error"] > 0){
			echo "Return Code: " . $_FILES["image"]["error"];
		   	http_response_code(500);
			exit();
	   }
	   else{
		   move_uploaded_file($_FILES["image"]["tmp_name"], $avatar);
	   }
	}
}

if(isset($data["delete_photo"]) && $data["delete_photo"] == 1){
	$avatar = "";
}

// ToDo remove previous image ----------------------------------------------------------------------------


// set user
// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET username = ?, name = ?, description = ?, avatar = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'ssssd', $username, $name, $description, $avatar, $jwt_payload["user_id"]);

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

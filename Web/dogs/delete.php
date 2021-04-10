<?
// REQUEST: ВУДУЕУ
// Headers {jwt:string}
// Query {id: int}
//
// ERRORS:
// 400:
// no id given;
//
// 401:
// no jwt token given; 
// incorrect token;
//
// 403:
// not an owner;
//
// 404:
// dog not found;
//
// 500:
// server error;

require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

// getting query
$data = $_GET;

// check query
if(!isset($data["id"])){
	http_response_code(400);
	exit();
}

// check if user is an owner
// preparing db request
$stmt = mysqli_prepare($link, "SELECT user_id FROM dogs WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $data["id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

mysqli_stmt_store_result($stmt);

// check if dog exists
if(mysqli_stmt_num_rows($stmt) != 1){
	echo "dog not found";
	http_response_code(404);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $user_id);

// getting db saved password
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);

if($user_id != $jwt_payload["user_id"]){
	http_response_code(403);
	exit();
}

// delete dog
// preparing db request
$stmt = mysqli_prepare($link, "DELETE FROM dogs WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $data["id"]);

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

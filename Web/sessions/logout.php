<?
// REQUEST: DELETE
// Headers {jwt: string}
//
// ERRORS:
//
// 401: 
// no jwt token given; 
// incorrect token; 
//
// 500:
// server error;

require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

// removing key
unset($jwt_check["keys_from_db"][array_search($jwt_check["key_found"], $jwt_check["keys_from_db"])]);

// preparing db request
$stmt = mysqli_prepare($link, "UPDATE users SET users.keys = ? WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'sd', implode(",", $jwt_check["keys_from_db"]), $jwt_payload["user_id"]);

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

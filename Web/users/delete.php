<?
// REQUEST: DELETE
// Headers {jwt: string}
//
// RETURN:
//
// ERRORS:
// 401:
// no jwt token given; 
// incorrect token; 
//
// 500:
// server error;

require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = checkJwt["jwt_payload"];

// delete user
// preparing db request
$stmt = mysqli_prepare($link, "DELETE FROM users WHERE user_id = ?");
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
?>
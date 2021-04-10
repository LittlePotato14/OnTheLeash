<?
// REQUEST: POST
// Headers {jwt:string}
// Body json {"name": string, "sex": int(0 - male, 1 - female), "birtday": string(yyyy-mm-dd), "breed": string, "castration": bool, "ready_to_mate": bool, "for_sale": bool}
//
// ERRORS:
// 401:
// no jwt token given; 
// incorrect token;
//
// 422:
// Incorrect date format;
//
// 500:
// server error;

require("../general_files/check_jwt.php");

defined('APP_RAN') or die();

// check jwt
$jwt_check = checkJwt($link);
$jwt_payload = $jwt_check["jwt_payload"];

// getting request body
$postData = file_get_contents('php://input');
$data = json_decode($postData, true);

// check body
if(!isset($data["name"]) || !isset($data["sex"]) || !isset($data["birthday"]) || !isset($data["breed"]) || !isset($data["castration"]) || !isset($data["ready_to_mate"]) || !isset($data["for_sale"])){
	http_response_code(400);
	exit();
}

if (!DateTime::createFromFormat('Y-m-d', $data["birthday"])) {
	http_response_code(422);
	exit();
}

// add dog to database
// preparing db request
$stmt = mysqli_prepare($link, "INSERT INTO dogs SET user_id = ?, name = ?, sex = ?, birthday = ?, breed = ?, castration = ?, ready_to_mate = ?, for_sale = ?");
mysqli_stmt_bind_param($stmt, 'dsdssddd', $jwt_payload["user_id"], $data["name"], $data["sex"], $data["birthday"], $data["breed"], $data["castration"], $data["ready_to_mate"], $data["for_sale"]);

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

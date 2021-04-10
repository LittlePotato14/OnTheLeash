<?
function checkJwt($link, $active=true) {
	// getting jwt token from header
	$headers = getallheaders();

	if(!isset($headers["jwt"])){
		echo "No jwt token given";
		http_response_code(401);
		exit();
	}

	$jwt = $headers["jwt"];

	// decoding
	$jwt_splited_token = explode(".", $jwt);
	$json_jwt_header = base64_decode(str_replace(['-', '_'], ['+', '/'], $jwt_splited_token[0]));
	$jwt_header = json_decode($json_jwt_header, true);
	$json_jwt_payload = base64_decode(str_replace(['-', '_'], ['+', '/'], $jwt_splited_token[1]));
	$jwt_payload = json_decode($json_jwt_payload, true);
	$jwt_signature = base64_decode(str_replace(['-', '_'], ['+', '/'], $jwt_splited_token[2]));

	$unsigned_token = $jwt_splited_token[0] . "." . $jwt_splited_token[1];
	
	// check for correct payload
	if(!isset($jwt_payload["user_id"]) || !isset($jwt_payload["active"]) || ($active != $jwt_payload["active"])){
		echo "Incorrect token";
		http_response_code(401);
		exit();
	}

	// check for correct signature
	// preparing db request
	$stmt = mysqli_prepare($link, "SELECT `users`.keys FROM users WHERE id = ?");
	mysqli_stmt_bind_param($stmt, 'd', $jwt_payload["user_id"]);

	// executing db request
	if(mysqli_stmt_execute($stmt));
	else{
		echo var_dump(mysqli_stmt_error($stmt));
		http_response_code(500);
		exit();
	}

	// associating result columns with variables
	mysqli_stmt_bind_result ($stmt , $keys_from_db);

	// getting db saved keys
	mysqli_stmt_fetch ($stmt); 

	// closing statement
	mysqli_stmt_close ($stmt);

	$key_found = false;

	$keys_from_db = explode(",", $keys_from_db);

	// checking each key
	foreach ($keys_from_db as $value){
		if(hash_hmac('sha256', $unsigned_token, $value, true) == $jwt_signature){
			$key_found = $value;
			break;
		}
	}

	// bad token
	if(!$key_found){
		echo "Incorrect token";
		http_response_code(401);
		exit();
	}
	
	return ["jwt_payload"=>$jwt_payload, "key_found"=>$key_found, "keys_from_db"=>$keys_from_db];
}
?>

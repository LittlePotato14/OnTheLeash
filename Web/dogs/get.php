<?
// REQUEST: GET
// Query {id: int?}
//
// RETURN:
// json {"name": string, "sex": int(0 - male, 1 - female), "birtday": string(yyyy-mm-dd), "breed": string, "castration": bool, "ready_to_mate": bool, "for_sale": bool}
//
// ERRORS:
// 400:
// no id given;
//
// 404: 
// dog not found;
//
// 500:
// server error;

defined('APP_RAN') or die();

// getting query
$data = $_GET;

// check query
if(!isset($data["id"])){
	http_response_code(400);
	exit();
}

// get dog from db
// preparing db request
$stmt = mysqli_prepare($link, "SELECT name, sex, birthday, breed, castration, ready_to_mate, for_sale FROM dogs WHERE id = ?");
mysqli_stmt_bind_param($stmt, 'd', $data["id"]);

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

mysqli_stmt_store_result($stmt);

// check if id exists
if(mysqli_stmt_num_rows($stmt) != 1){
	echo "dog not found";
	http_response_code(404);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $name, $sex, $birthday, $breed, $castration, $ready_to_mate, $for_sale);

// getting dog info
mysqli_stmt_fetch ($stmt); 

// closing statement
mysqli_stmt_close ($stmt);


// response json
echo json_encode(["name"=> $name, "sex"=> $sex, "birthday"=> $birthday, "breed"=> $breed, "castration"=> $castration, "ready_to_mate"=> $ready_to_mate, "for_sale"=> $for_sale]);
?>
<?
// REQUEST: GET
//
// RETURN:
// json [String, ...]
//
// ERRORS:
// 500:
// server error;

require("../general_files/config.php");

// preparing db request
$stmt = mysqli_prepare($link, "SELECT name FROM breeds");

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// associating result columns with variables
mysqli_stmt_bind_result ($stmt , $name);

$breeds = [];

// getting breeds
while(mysqli_stmt_fetch ($stmt)){
	$breeds[] = $name;
}	

// closing statement
mysqli_stmt_close ($stmt);

echo json_encode($breeds);

require("../general_files/end_config.php");
?>
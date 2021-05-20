<?
require("general_files/config.php");

// delete user
// preparing db request
$stmt = mysqli_prepare($link, "DELETE FROM users WHERE confirmed = 0 AND registration_date < ?");
mysqli_stmt_bind_param($stmt, 's', date('Y-m-d H:i:s', strtotime('-24 hour')));

// executing db request
if(mysqli_stmt_execute($stmt));
else{
	echo var_dump(mysqli_stmt_error($stmt));
	http_response_code(500);
	exit();
}

// closing statement
mysqli_stmt_close ($stmt);

require("general_files/end_config.php");
?>
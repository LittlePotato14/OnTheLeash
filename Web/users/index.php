<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        require("registration.php");
        break;
	case 'DELETE':
		require("delete.php");
		break;
	case 'GET':
		require("get.php");
		break;
	case 'PUT':
		// getting request body
		$postData = file_get_contents('php://input');
		$data = json_decode($postData, true);

		if(isset($data["email"])){
			require("update_email.php");
		}
		if(isset($data["password"])){
			require("update_password.php");
		}
		if(isset($data["latitude"]) && isset($data["longitude"])){
			require("update_location.php");
		}
		
		break;
}

require("../general_files/end_config.php");
?>
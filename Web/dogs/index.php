<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        require("create.php");
        break;
	case 'DELETE':
		require("delete.php");
		break;
	case 'GET':
		require("get.php");
		break;
	case 'PUT':
		require("update.php");
		break;
}

require("../general_files/end_config.php");
?>
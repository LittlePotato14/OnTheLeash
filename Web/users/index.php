<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        require("registration.php");
        break;
	case 'DELETE':
		require("delete.php");
		break;
}

require("../general_files/end_config.php");
?>
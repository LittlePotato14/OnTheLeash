<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        require("new.php");
        break;
    case 'PUT':
        require("confirmation.php");
        break;
}

require("../general_files/end_config.php");
?>
<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        require("login.php");
        break;
    case 'DELETE':
        require("logout.php");
        break;
}

require("../general_files/end_config.php");
?>
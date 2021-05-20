<?
require("../general_files/config.php");

switch ($_SERVER['REQUEST_METHOD']) {
	case 'GET':
		// getting query
		$data = $_GET;
		
		if(isset($data["id"])){
			require("get_marker.php");
		}
		else{
			require("get_all_markers.php");
		}
		
		break;
}

require("../general_files/end_config.php");
?>
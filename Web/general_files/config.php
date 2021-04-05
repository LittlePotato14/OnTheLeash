<?
require_once("passwords.php");

$link = mysqli_connect($config_host, $config_user, $config_password, $config_db);

if (!$link) {
    echo "Ошибка: Невозможно установить соединение с MySQL." . PHP_EOL;
    echo "Код ошибки errno: " . mysqli_connect_errno() . PHP_EOL;
    echo "Текст ошибки error: " . mysqli_connect_error() . PHP_EOL;	http_response_code(500);
    exit;
}

//echo "Соединение с MySQL установлено!" . PHP_EOL;
//echo "Информация о сервере: " . mysqli_get_host_info($link) . PHP_EOL;

define('APP_RAN', true);
?>
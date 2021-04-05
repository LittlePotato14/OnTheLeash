<? 
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

require 'source/PHPMailer/Exception.php';
require 'source/PHPMailer/PHPMailer.php';
require 'source/PHPMailer/SMTP.php';

$mail = new PHPMailer();
$mail->isSMTP();
$mail->CharSet = "utf-8";
$mail->SMTPDebug = SMTP::DEBUG_SERVER;
$mail->Debugoutput = 'html';
$mail->Host = "smtp.yandex.ru";
$mail->Port = 465;
$mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
$mail->SMTPAuth = true;
$mail->Username = "no-reply@na-povodke.ru";
$mail->Password = "Yf2DeW&YH4eLJ&E";
$mail->setFrom('no-reply@na-povodke.ru', 'na-povodke.ru');
$mail->addAddress($_GET["email"], $_GET["email"]);
$mail->Subject = 'Подтверждение регистрации na-povodke';
$mail->Body = "Код подтверждения: " . "works";
$mail->isHTML(true);

//send the message, check for errors
if (!$mail->send()) {
    echo 'Mailer Error: ' . $mail->ErrorInfo;
} else {
    echo 'Message sent!';
}
?>
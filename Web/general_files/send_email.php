<?
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;
	
function sendEmail($email, $subject, $body){
	require '../source/PHPMailer/Exception.php';
	require '../source/PHPMailer/PHPMailer.php';
	require '../source/PHPMailer/SMTP.php';
	require ("../general_files/passwords.php");

	$mail = new PHPMailer();
	$mail->isSMTP();
	$mail->CharSet = "utf-8";
	$mail->SMTPDebug = 0;
	$mail->Debugoutput = 'html';
	$mail->Host = "smtp.yandex.ru";
	$mail->Port = 465;
	$mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
	$mail->SMTPAuth = true;
	$mail->Username = $mail_username;
	$mail->Password = $mail_password;
	$mail->setFrom($mail_username, $mail_from);
	$mail->addAddress($email, $email);
	$mail->Subject = $subject;
	$mail->Body = $body;
	$mail->isHTML(true);
	
	//send the message, check for errors
	if (!$mail->send()) {
		 file_put_contents("mail_error.txt", 'Mailer Error: ' . $mail->ErrorInfo);
	}
}
?>
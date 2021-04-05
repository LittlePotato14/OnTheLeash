<?
function generateJwt($user_id, $secret_key, $active=true) {
    // header
	$jwt_header = json_encode(['alg'=> 'HS256', 'typ'=> 'JWT']);
	$base64UrlHeader = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($jwt_header));

	// payload
	$jwt_payload = json_encode(['user_id'=>$user_id, 'active'=>$active]);
	$base64UrlPayload = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($jwt_payload));
	
	// signature
	$unsigned_token = $base64UrlHeader . "." . $base64UrlPayload;
	$signature = hash_hmac('sha256', $unsigned_token, $secret_key, true);
	$base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));
	
	// jwt
	$jwt = $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
	
	return $jwt;
}
?>
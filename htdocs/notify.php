<?php
require_once "init.php";
    error_log("POST: ".print_r($_POST,true));


if (!isset($_POST["publickey"])) errorResult(400, "pubkey missing");

$pubKeys = $_POST["publickey"];

if (count($pubKeys) == 0 || count($pubKeys) > 10) errorResult(400, "pubkey missing");

foreach($pubKeys as $key) {
    $stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
    $stmt->execute(array($key));

    $info = $stmt->fetch(PDO::FETCH_ASSOC);
    error_log("requesting info for key=\"$key\"");

    if (!$info) {
        errorResult(400, "invalid pubkey");
    }

    error_log("success: recipient found with pkhash=".$info["publickey_hash"].", nick=$info[nickname], tel=$info[phone]");

    $url = 'https://android.googleapis.com/gcm/send';
    $data = array(
        "registration_ids" => array($info["gcmid"]),
     //   "data" => array("msg" => utf8_encode($_POST["msg"]))
        "data" => array("msg" => base64_encode($_POST["msg"]))
    );
    //error_log(bin2hex($_POST["msg"]));
    $data =json_encode($data);
    //error_log($data);
    // use key 'http' even if you send the request to https://...
    $options = array(
        'http' => array(
            'header'  => "Content-type: application/json\r\nAuthorization:key=AIzaSyCeVpYONn_oHHPOkr5UTGXmQ_mabp4ArY8\r\nContent-Length: ".strlen($data)."\r\n",
            'method'  => 'POST',
            'content' => $data,
            'ignore_errors' => true
        ),
    );
    $context  = stream_context_create($options);
    $result = file_get_contents($url, false, $context);
    $res = json_decode($result);
    if ($res->success == 1) {
      error_log("success: message sent ". print_r($result,true));
      echo "OK";
    } else {
      errorResult(500, $result);
    }
    
}

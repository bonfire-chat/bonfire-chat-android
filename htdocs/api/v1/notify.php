<?php
require_once "init.php";
    error_log("POST: recipientPublicKey=$_POST[recipientPublicKey], nextHopId=$_POST[nextHopId], senderId=$_POST[senderId], msgLen=".strlen($_POST["msg"]));

if (!isset($_POST["recipientPublicKey"])) errorResult(400, "pubkey missing");

$key = $_POST["recipientPublicKey"];
$nextHop = @$_POST["nextHopId"];


if ($nextHop) {
    $stmt = $db->prepare("SELECT * FROM users WHERE id = ?");
    $stmt->execute(array($nextHop));
    error_log("requesting info for nextHop with id=\"$nextHop\"");
} else {
    $stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
    $stmt->execute(array($key));
    error_log("requesting info for key=\"$key\"");
}

    $info = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$info) {
        errorResult(400, "invalid pubkey");
    }

    if (preg_match("/[^a-z0-9-]/", $_POST["uuid"])) errorResult(400, "Invalid uuid");
    file_put_contents(DATA_FOLDER."/message-$_POST[uuid].dat", $_POST["msg"]);
    
    error_log("success: recipient found with pk=".$info["publickey"].", nick=$info[nickname], tel=$info[phone]");
    
    $sender_id = sprintf("%06d", intval($_POST["senderId"]));
    
    $encoded_msg = base64_encode($_POST["msg"]);
    error_log("Message length ".strlen($encoded_msg));
    
    if (strlen($encoded_msg) > 3800)
        $data = array("uuid" => $_POST["uuid"], "senderId" => $sender_id);
    else
        $data = array("msg" => $encoded_msg, "senderId" => $sender_id);
    
    $url = 'https://android.googleapis.com/gcm/send';
    $data = array(
        "registration_ids" => array($info["gcmid"]),
     //   "data" => array("msg" => utf8_encode($_POST["msg"]))
        "data" => $data
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

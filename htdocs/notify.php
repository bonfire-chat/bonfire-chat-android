<?php
require_once "init.php";

$stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
$stmt->execute(array($_POST["publickey"]));

$info = $stmt->fetch(PDO::FETCH_ASSOC);
if (!$info) die("invalid pubkey");

$url = 'https://android.googleapis.com/gcm/send';
$data = array(
    "registration_ids" => array($info["gcmid"]),
    "data" => array("msg" => $_POST["msg"])
        );
$data =json_encode($data);
// use key 'http' even if you send the request to https://...
$options = array(
        'http' => array(
                'header'  => "Content-type: application/json\r\nAuthorization:key=AIzaSyCeVpYONn_oHHPOkr5UTGXmQ_mabp4ArY8\r\nContent-Length: ".strlen($data)."\r\n",
                'method'  => 'POST',
                'content' => $data,
            ),
    );
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

var_dump($result);


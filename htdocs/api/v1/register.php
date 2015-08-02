<?php
require_once "init.php";

if (!isset($_POST["nonce"]) || !isset($_POST["publickey"]) || !isset($_POST["data"])) {
  errorResult(400, "Missing parameters");
}

$pubkey = $_POST["publickey"];

$kp = \Sodium\crypto_box_keypair_from_secretkey_and_publickey(hex2bin(SECRETKEY), base64url_decode($pubkey));
$bin_data = base64url_decode($_POST["data"]);
$bin_nonce = base64url_decode($_POST["nonce"]);
$decr = \Sodium\crypto_box_open($bin_data, $bin_nonce, $kp);
parse_str($decr, $data);
if (!$data) errorResult(400, "Validation error");

if (!isset($data["nickname"]) || !isset($data["phone"]) || !isset($data["gcmid"])) {
   errorResult(400, "Missing body parameters");
}

$stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
$stmt->execute(array($pubkey));
if ($stmt->rowCount()) {
    $info = $stmt->fetch();
    error_log("updating public key with hash $pubkey ...");
    $up = $db->prepare("UPDATE users SET nickname=?, phone=?, gcmid=?, last_updated=NOW(), ip=? WHERE id =?");
    $ok = $up->execute(array( $data["nickname"], $data["phone"], $data["gcmid"], $_SERVER["REMOTE_ADDR"], $info['id']));
    if (!$ok) { error_log(print_r($up->errorInfo(),true)); errorResult(500, "Error: ".$up->errorInfo()[2]); }
    echo "ok";
    
} else {
    error_log("inserting record pkhash=$pubkey  nick=$data[nickname]");
    $stmt = $db->prepare("INSERT INTO users (nickname, publickey, phone, gcmid, created, last_updated, ip) VALUES(?,?,?,?,?,NOW(),NOW(),?)");
    $ok=$stmt->execute(array($data["nickname"], $pubkey, $data["phone"], $data["gcmid"], $_SERVER["REMOTE_ADDR"]));
    if (!$ok) { error_log(print_r($stmt->errorInfo(),true)); errorResult(500, "Error: ".$stmt->errorInfo()[2]); }
    echo "ok";
}






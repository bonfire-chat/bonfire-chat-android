<?php
require_once "init.php";

if (!isset($_POST["xmppid"]) || !isset($_POST["publickey"]) || !isset($_POST["phone"])
    || !isset($_POST["gcmid"])) {
  errorResult(400, "Missing parameters");
}

$pkhash = base64_encode(md5(base64url_decode($_POST["publickey"]), true));

$stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
$stmt->execute(array($_POST["publickey"]));
if ($stmt->rowCount()) {
    $info = $stmt->fetch();
    error_log("updating public key with hash $pkhash ...");
    $up = $db->prepare("UPDATE users SET xmppid=?, publickey=?, publickey_hash=?, phone=?, gcmid=?, last_updated=NOW(), ip=? WHERE id =?");
    $up->execute(array($_POST["xmppid"], $_POST["publickey"], $pkhash, $_POST["phone"], $_POST["gcmid"], $_SERVER["REMOTE_ADDR"], $info['id']));
    
} else {
    error_log("inserting record pkhash=$pkhash  nick=$_POST[nickname]");
    $stmt = $db->prepare("INSERT INTO users (nickname, xmppid, publickey, publickey_hash, phone, gcmid, created, last_updated, ip) VALUES(?,?,?,?,?,?,NOW(),NOW(),?)");
    $stmt->execute(array($_POST["nickname"], $_POST["xmppid"], $_POST["publickey"], $pkhash, $_POST["phone"], $_POST["gcmid"], $_SERVER["REMOTE_ADDR"]));
    if ($stmt->errorCode()) { error_log(print_r($stmt->errorInfo(),true)); errorResult(500, "SQL Error ".$stmt->errorCode()); }
    echo str_replace("\n","  ",print_r($stmt->errorInfo(),true));
}






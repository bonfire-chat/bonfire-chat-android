<?php
require_once "init.php";

$stmt = $db->prepare("SELECT * FROM users WHERE publickey = ?");
$stmt->execute(array($_POST["publickey"]));
if ($stmt->rowCount()) {
    $info = $stmt->fetch();
    $up = $db->prepare("UPDATE users SET xmppid=?, publickey=?, phone=?, gcmid=?, last_updated=NOW(), ip=? WHERE id =?");
    $up->execute(array($_POST["xmppid"], $_POST["publickey"], $_POST["phone"], $_POST["gcmid"], $_SERVER["REMOTE_ADDR"], $info['id']));
    
} else {
    $stmt = $db->prepare("INSERT INTO users (nickname, xmppid, publickey, phone, gcmid, created, last_updated, ip) VALUES(?,?,?,?,?,NOW(),NOW(),?)");
    $stmt->execute(array($_POST["nickname"], $_POST["xmppid"], $_POST["publickey"], $_POST["phone"], $_POST["gcmid"], $_SERVER["REMOTE_ADDR"]));
    echo str_replace("\n","  ",print_r($stmt->errorInfo(),true));
}






<?php
require_once "init.php";

header("Content-Type: application/json");

$stmt = $db->prepare("SELECT nickname,xmppid,publickey,phone,last_updated FROM users WHERE nickname LIKE ? ORDER BY last_updated desc");
$stmt->execute(array($_GET["nickname"]));

echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));


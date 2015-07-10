<?php
require_once "init.php";

header("Content-Type: application/json");

if (!isset($_GET["nickname"])) {
  header("HTTP/1.1 400 Bad Request");
  die(json_encode(array("error" => "Bad request")));
}

$stmt = $db->prepare("SELECT nickname,xmppid,publickey,phone,last_updated FROM users WHERE nickname LIKE ? ORDER BY last_updated desc");
$stmt->execute(array($_GET["nickname"]));

echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));


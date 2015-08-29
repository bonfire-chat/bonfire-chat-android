<?php
require_once "init.php";

header("Content-Type: application/json");

if (!isset($_POST["numbers"])) {
  header("HTTP/1.1 400 Bad Request");
  die(json_encode(array("error" => "Bad request")));
}

$nums = explode(",", $_POST["numbers"]);
foreach($nums as $num ) {
  $num = preparePhonenumber($num);
  //error_log("search number $num");
  $stmt = $db->prepare("SELECT publickey FROM users WHERE phone = ? ORDER BY last_updated desc LIMIT 1");
  $stmt->execute(array($num));
  $result = $stmt->fetch(PDO::FETCH_ASSOC);
  if ($result) echo "$result[publickey]\n"; else echo "\n";
}



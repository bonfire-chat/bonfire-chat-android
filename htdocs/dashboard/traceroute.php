<?php
require "../api/v1/init.php";
header("Content-Type: application/json");
  
if (isset($_GET["uuid"])) {
  $stmt = $db->prepare("SELECT * FROM traceroutes WHERE uuid  = ? ORDER BY client_ts ASC");
  $stmt->execute(array($_GET["uuid"]));
  $info = $stmt->fetchAll();
  echo json_encode($info);
} else {
  $stmt = $db->prepare("SELECT DISTINCT uuid FROM traceroutes ORDER BY insert_ts DESC");
  $stmt->execute();
  $info = $stmt->fetchAll();
  echo json_encode($info);
}


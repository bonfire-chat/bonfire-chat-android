<?php
require "init.php";
header("Content-Type: application/json");
  
if (count($_POST)) {
  $stmt = $db->prepare("INSERT INTO traceroutes (uuid, traceroute, client_ts, action, peer, protocol, reporter) VALUES(?,?,?,?,?,?,?)");
  //if (isset($_POST["string"])) $_POST["traceroute"] = "<span><time>$_POST[datetime]</time> <b>$_POST[action] $_POST[peer]</b> via $_POST[protocol] (reported by $_POST[reporter])</span> $_POST[string]";
  $stmt->execute(array($_POST["uuid"], $_POST["string"], $_POST["datetime"], $_POST["action"], $_POST["peer"], $_POST["protocol"], $_POST["reporter"]));
  header("HTTP/1.1 204 No Content");
} else if (isset($_GET["uuid"])) {
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



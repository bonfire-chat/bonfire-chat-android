<?php
require "init.php";
header("Content-Type: application/json");
  
if (count($_POST)) {
  $stmt = $db->prepare("INSERT INTO traceroutes (uuid, traceroute, client_ts, action, peer, protocol, reporter, hop_from, hop_to) 
      VALUES(?,?,?,?,?,?,?,?,?)");
  //if (isset($_POST["string"])) $_POST["traceroute"] = "<span><time>$_POST[datetime]</time> <b>$_POST[action] $_POST[peer]</b> via $_POST[protocol] (reported by $_POST[reporter])</span> $_POST[string]";
  $stmt->execute(array($_POST["uuid"], $_POST["string"], $_POST["datetime"], $_POST["action"], $_POST["peer"],
      $_POST["protocol"], $_POST["reporter"], $_POST["hop1"], $_POST["hop2"]));
  
  header("HTTP/1.1 204 No Content");
}



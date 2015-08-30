<?php
require "../api/v1/init.php";
header("Content-Type: application/json");
  
if (isset($_GET["uuid"])) {
  $stmt = $db->prepare("SELECT * FROM traceroutes WHERE uuid  = ? ORDER BY client_ts ASC");
  $stmt->execute(array($_GET["uuid"]));
  $info = $stmt->fetchAll();
  echo json_encode($info);
} else {
  $stmt = $db->prepare("SELECT uuid, 
      (SELECT reporter from traceroutes c WHERE c.uuid=p.uuid AND action='SEND' AND traceroute like '%hopCount=0%' ORDER BY insert_ts ASC LIMIT 1) sender 
      FROM traceroutes p GROUP BY uuid ORDER BY insert_ts DESC");
  $stmt->execute();
  $info = $stmt->fetchAll();
  echo json_encode($info);
}


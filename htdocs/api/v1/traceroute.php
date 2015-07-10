<?php
require "init.php";

if (count($_POST)) {
  $stmt = $db->prepare("INSERT INTO traceroutes (uuid, traceroute) VALUES(?,?)");
  $stmt->execute(array($_POST["uuid"], $_POST["traceroute"]));
} else {
  $stmt = $db->prepare("SELECT * FROM traceroutes WHERE uuid  = ?");
  $stmt->execute(array($_GET["uuid"]));
  $info = $stmt->fetchAll();
  echo "<meta name='viewport' content='width=device-width'>";
  if (!$info) {
     echo "<script>setTimeout(function(){location.reload()}, 1000);</script> Eile mit Weile";
     
  } else {
    echo "<pre>";
    foreach($info as $d)
     echo "<hr>". $d["traceroute"];
  }
}



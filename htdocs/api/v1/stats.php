<?php

require "init.php";

if (isset($_POST["timestamp"])) {
  $stmt = $db->prepare("INSERT INTO stats (`timestamp`, `publickey`, `batterylevel`, `powerusage`, `messages_sent`, `messages_received`, lat, lng)
    VALUES(?,?,?,?,?,?,?,?)");

  $ok=$stmt->execute(array(
    $_POST["timestamp"], $_POST["publickey"], $_POST["batterylevel"], $_POST["powerusage"], $_POST["messages_sent"], $_POST["messages_received"], $_POST["lat"], $_POST["lng"]
  ));

  if (!$ok) { error_log(print_r($stmt->errorInfo(),true)); errorResult(500, "Error: ".$stmt->errorInfo()[2]); }
  echo "ok";
}

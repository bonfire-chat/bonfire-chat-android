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
} else {
  header("Content-Type: application/json");
  if(!isset($_GET["mode"])) die(json_encode(array("error" => "missing mode parameter")));
  
  if ($_GET["mode"] == "chart") {
    $query = "SELECT date(timestamp) statdate, hour(timestamp) stathour,timestamp,
    min(batterylevel) min_batterylevel,min(powerusage) min_powerusage,min(messages_sent) min_messages_sent,
    min(messages_received) min_messages_received,min(lat) min_lat,min(lng) min_lng,
    avg(batterylevel) avg_batterylevel,avg(powerusage) avg_powerusage,avg(messages_sent) avg_messages_sent,
    avg(messages_received) avg_messages_received,avg(lat) avg_lat,avg(lng) lng,
    max(batterylevel) max_batterylevel,max(powerusage) max_powerusage,max(messages_sent) max_messages_sent,
    max(messages_received) max_messages_received,max(lat) max_lat,max(lng) max_lng ,
    sum(messages_received) sum_messages_received, sum(messages_sent) sum_messages_sent
    FROM stats GROUP BY date(timestamp),hour(timestamp);  ";
  } elseif ($_GET["mode"] == "msgs") {
    $query = "SELECT date(timestamp) statdate, hour(timestamp) stathour,timestamp,if(nickname is null,stats.publickey,nickname) name,
    max(messages_received) max_messages_received, max(messages_sent) messages_sent
    FROM stats left outer join users on users.publickey=stats.publickey
    GROUP BY date(timestamp),hour(timestamp),stats.publickey;  ";
  } elseif($_GET["mode"] == "raw") {
    $query = "SELECT * FROM stats";
  } elseif ($_GET["mode"] == "devices") {
    $query = "SELECT timestamp, stats.publickey, if(nickname is null,stats.publickey,nickname) name, batterylevel, powerusage, messages_sent,
    messages_received, lat, lng FROM stats LEFT OUTER JOIN users ON users.publickey=stats.publickey
    WHERE timestamp=(select max(s.timestamp) from stats s where s.publickey=stats.publickey)
    GROUP BY stats.publickey";
    //order by timestamp desc ;";
  } elseif ($_GET["mode"] == "device" && isset($_GET["pk"])) {
    $query = "SELECT timestamp, publickey, batterylevel, powerusage, messages_sent,
    messages_received, lat, lng FROM stats
    WHERE publickey = '".mysql_escape_string($_GET["pk"])."'
    order by timestamp desc
    ;";
  }
  $stmt = $db->prepare($query);
  $stmt->execute();
  if ($stmt->errorCode()>0) errorResult(500, 'ERR: '.$stmt->errorInfo()[2]);
  $result = $stmt->fetchAll(PDO::FETCH_ASSOC);
  echo json_encode($result);


}

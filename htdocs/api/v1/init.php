<?php
include ".htconfig.php";
if (!$DB_USER) die("Please copy htconfig_template.php to .htconfig.php and fill in the settings");
ini_set("display_errors","on");
$db = new PDO("mysql:host=$DB_HOST;dbname=$DB_NAME;charset=utf8", $DB_USER, $DB_PASS);
$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

function errorResult($code, $msg) {
  if ($code == 400) header("HTTP/1.1 400 Bad Request"); else header("HTTP/1.1 500 Internal Server Error");
  echo "$msg\n";
  error_log("ERROR: $msg");
  exit;
}

function base64url_decode($base64url) {
  $base64 = strtr($base64url, '-_', '+/');
  $plainText = base64_decode($base64);
  return ($plainText);
}

function preparePhonenumber($num) {
  $num = preg_replace("/[^0-9]/", "", $num);
  // HACK ...es wird davon ausgegangen, dass alle Nummern ohne Landesvorwahl deutsche Nummern sind
  $num = preg_replace("/^0/", "49", $num);
  return $num;
}
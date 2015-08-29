<?php
require_once "init.php";
header("Content-Type: application/octet-stream");


readfile(DATA_FOLDER."/message-$_GET[retrieve].dat");

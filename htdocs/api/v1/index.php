<?php

define("BASE", "https://bonfire.projects.teamwiki.net/api/v1/");

echo "<pre>";
echo json_encode(array(
	"version" => "1.0.3",
	"documentation" => "see source code repo",
	"links" => array(
		"user registration" => BASE."register",
		"user search" => BASE."search",
		"publish and query traceroutes" => BASE."traceroute",
		"Google Cloud Messaging notification" => BASE."notify",
		"device statistics" => BASE."stats"
		)
	), JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

#!/usr/bin/env php5
<?php

echo trim(strtr(base64_encode(hex2bin($argv[1])), '+/', '-_'),'=')."\n";
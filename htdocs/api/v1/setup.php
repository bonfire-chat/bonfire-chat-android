<pre><?php


$keypair = Sodium::crypto_box_keypair();

$sk = Sodium::crypto_box_secretkey($keypair);

$pk = Sodium::crypto_box_publickey($keypair);



echo "SK: "; var_dump(bin2hex($sk));
echo "PK: "; var_dump(bin2hex($pk));

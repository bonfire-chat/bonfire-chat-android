
#include <sodium.h>
#include <string.h>

void encrypt(char* string, char* pk_hex, char* sk_hex);
void generate();

int main(int argc, char** argv) {
  if (sodium_init() == -1) {
    return 1;
  }

  if (!strcmp(argv[1], "encrypt")) {
    encrypt(argv[2], argv[3], argv[4]);
  } else if (!strcmp(argv[1], "keygen")) {
    generate();
  } else {
    printf("Usage: %s [encrypt|keygen]\n", argv[0]);
  }
}

void encrypt(char* string, char* pk_hex, char* sk_hex) {
  unsigned char pk[crypto_box_PUBLICKEYBYTES];
  unsigned char sk[crypto_box_SECRETKEYBYTES];
  
  sodium_hex2bin(pk, crypto_box_PUBLICKEYBYTES, pk_hex, strlen(pk_hex), NULL, NULL, NULL);
  sodium_hex2bin(sk, crypto_box_SECRETKEYBYTES, sk_hex, strlen(sk_hex), NULL, NULL, NULL);

  unsigned char nonce[crypto_box_NONCEBYTES];
  int len = crypto_box_MACBYTES + strlen(string);
  unsigned char* ciphertext = (char*)malloc(crypto_box_MACBYTES + strlen(string));
  randombytes_buf(nonce, sizeof nonce);
  crypto_box_easy(ciphertext, string, strlen(string), nonce, pk, sk);

  unsigned char* hex = (char*)malloc(len);
  sodium_bin2hex(hex, 2*len, ciphertext, len);
  printf("ciphertext: %s\n", hex);
  char hex2[48];
  sodium_bin2hex(hex2, 48, nonce, 24);
  printf("nonce: %s\n", hex2);
}

void generate() {
  unsigned char pk[crypto_box_PUBLICKEYBYTES];
  unsigned char sk[crypto_box_SECRETKEYBYTES];
  
  crypto_box_keypair(pk, sk);
  
  char hex[80];
  sodium_bin2hex(hex, 80, sk, crypto_box_SECRETKEYBYTES);
  printf("sk: %s\n", hex);
  sodium_bin2hex(hex, 80, pk, crypto_box_PUBLICKEYBYTES);
  printf("pk: %s\n", hex);
  
}


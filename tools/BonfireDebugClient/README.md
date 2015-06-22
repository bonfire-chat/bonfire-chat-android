# BonfireDebugClient

Ein Konsolenclient für das Bonfire GcmProtocol. Unterstützt das verschlüsselte Senden von Nachrichten.

## Usage

```
# Download the server certificate and save to file
echo | openssl s_client -connect bonfire.projects.teamwiki.net:443 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > server.pem

# Generate a key and store nickname
java -jar BonfireDebugClient.jar keygen MyNickName

# Send Message (insert the recipient's public key)
java -jar BonfireDebugClient.jar mwq7WUkFRZs4wUgPpbrIy2K2wrm4sXna53zmlZCiDAs "Hello, world."
```





package de.wikilab.bonfiredebug;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PublicKey;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		String command = args.length < 1 ? "" : args[0];
		if (command.equals("keygen")) {
			KeyPair kp = new KeyPair();
			PrintWriter out = new PrintWriter("bonfire_secret.hex");
			out.println(kp.getPrivateKey().toString());
			out.println(args[1]);
			out.close();
		} else if (command.equals("search")) {


		} else if (command.equals("send")) {
			Scanner s = new Scanner(new File("bonfire_secret.hex"));
			String secretKey = s.nextLine();
			String nickname = s.nextLine();
			KeyPair sk = new KeyPair(secretKey, Encoder.HEX);
			System.out.println("Sender Public Key: "+sk.getPublicKey().toString());

			PublicKey pk = new PublicKey(CryptoHelper.fromBase64(args[1]));
			System.out.println("Recipient Key: "+pk.toString());

			Envelope envelope = new Envelope(pk.toBytes(), nickname, sk, args[2]);
			sendHTTP(envelope);

		} else {
			System.err.println("Usage: ");
			System.err.println("  java -jar BonfireDebugClient.jar COMMAND [OPTS]");
			System.err.println("Commands:");
			System.err.println("  keygen NICK");
			System.err.println("  send TARGETKEY MESSAGE");
		}

    }

	protected static void sendEnvelope(OutputStream output, Envelope envelope) {
		try {
			//Log.d("SocketProtocol", "Sending envelope  uuid=" + envelope.uuid.toString() + "   from=" + envelope.senderNickname);
			ObjectOutputStream stream = new ObjectOutputStream(output);
			stream.writeObject(envelope);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream stream2 = new ObjectOutputStream(os);
			stream2.writeObject(envelope);
			System.err.println(HexDump.dumpHexString(os.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void sendHTTP(Envelope envelope) {
		HttpsURLConnection con = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			final Collection<? extends Certificate> certs =
					certFactory.generateCertificates(new FileInputStream("server.pem"));

			keyStore.setCertificateEntry("server", certs.iterator().next());

			TrustManagerFactory tmf =
					TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, tmf.getTrustManagers(), null);


			String url = "https://bonfire.projects.teamwiki.net/notify.php";
			URL obj = new URL(url);
			con = (HttpsURLConnection) obj.openConnection();
			con.setSSLSocketFactory(ctx.getSocketFactory());

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "BonfireDebugClient");
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=Je8PPsja_x");


			String urlParameters = "--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"publickey[]\"\r\n\r\n"
					+ CryptoHelper.toBase64(envelope.recipientsPublicKeys.get(0))
					+ "\r\n--Je8PPsja_x\r\n" +
					"Content-Disposition: form-data; name=\"msg\"\r\n" +
					"\r\n";
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			sendEnvelope(wr, envelope);
			wr.writeBytes("\r\n");
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("Sending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			String response = readStream(con.getInputStream());
			//print result
			System.out.println(response.toString());

		} catch(Exception ex) {
			if (con != null) {
				InputStream err = con.getErrorStream();
				if (err != null) {
					try {
						System.out.println(readStream(err));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			System.err.println("HTTP failed: " + ex.toString());
		}
	}

	public static String readStream(InputStream inputStream) throws IOException {

		BufferedReader in = new BufferedReader(
				new InputStreamReader(inputStream));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
}

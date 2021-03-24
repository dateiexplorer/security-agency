import org.json.JSONObject;
import utils.Key;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Base64;

public class RSAAlgorithm {

    private static RSAAlgorithm instance = new RSAAlgorithm();

    // Port
    public Port port;

    private RSAAlgorithm() {
        port = new Port();
    }

    public static RSAAlgorithm getInstance() {
        return instance;
    }

    private Key readKeyFromFile(File keyfile, String key) throws IOException {
        StringBuilder jsonData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(keyfile));

        String line = null;
        while (null != (line = reader.readLine())) {
            jsonData.append(line);
        }

        JSONObject jsonObject = new JSONObject(jsonData.toString());
        JSONObject keyObject = jsonObject.getJSONObject(key);

        BigInteger modulus = keyObject.getBigInteger("modulus");
        BigInteger exponent = keyObject.getBigInteger("exponent");

        return new Key(modulus, exponent);
    }

    private BigInteger crypt(BigInteger message, Key key) {
        return message.modPow(key.getE(), key.getN());
    }

    private String innerEncrypt(String plainMessage, File publicKeyfile) {
        try {
            Key key = readKeyFromFile(publicKeyfile, "public");
            byte[] bytes = plainMessage.getBytes(Charset.defaultCharset());
            return Base64.getEncoder().encodeToString(crypt(new BigInteger(bytes), key).toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String innerDecrypt(String encryptedMessage, File privateKeyfile) {
        try {
            Key key = readKeyFromFile(privateKeyfile, "private");
            byte[] msg = crypt(new BigInteger(Base64.getDecoder().decode(encryptedMessage)), key).toByteArray();
            return new String(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public class Port implements IRSAAlgorithm {

        @Override
        public String encrypt(String plainMessage, File publicKeyfile) {
            return innerEncrypt(plainMessage, publicKeyfile);
        }

        @Override
        public String decrypt(String encryptedMessage, File privateKeyfile) {
            return innerDecrypt(encryptedMessage, privateKeyfile);
        }
    }
}

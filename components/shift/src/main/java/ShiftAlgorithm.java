import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class ShiftAlgorithm {

    private static ShiftAlgorithm instance = new ShiftAlgorithm();

    // Port
    public Port port;

    private ShiftAlgorithm() {
        port = new Port();
    }

    public static ShiftAlgorithm getInstance() {
        return instance;
    }

    private int readKeyFromFile(File keyfile) {
        try {
            StringBuilder jsonData = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(keyfile));

            String line = null;
            while (null != (line = reader.readLine())) {
                jsonData.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonData.toString());
            return jsonObject.getInt("key");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String innerEncrypt(String plainMessage, File keyfile) {
        StringBuilder stringBuilder = new StringBuilder();
        int key = readKeyFromFile(keyfile);

        for (int i = 0; i < plainMessage.length(); i++) {
            char character = (char) (plainMessage.codePointAt(i) + key);
            stringBuilder.append(character);
        }

        return stringBuilder.toString();
    }

    private String innerDecrypt(String encryptedMessage, File keyfile) {
        StringBuilder stringBuilder = new StringBuilder();
        int key = readKeyFromFile(keyfile);

        for (int i = 0; i < encryptedMessage.length(); i++) {
            char character = (char) (encryptedMessage.codePointAt(i) - key);
            stringBuilder.append(character);
        }

        return stringBuilder.toString();
    }

    public class Port implements IShiftAlgorithm {

        @Override
        public String encrypt(String plainMessage, File keyfile) {
            return innerEncrypt(plainMessage, keyfile);
        }

        @Override
        public String decrypt(String encryptedMessage, File keyfile) {
            return innerDecrypt(encryptedMessage, keyfile);
        }
    }
}

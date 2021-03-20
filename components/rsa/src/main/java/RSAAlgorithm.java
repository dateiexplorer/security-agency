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

    private Key readKeyFromFile(File keyfile) throws IOException {
        InputStream input = new FileInputStream(keyfile);
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(input));

        try {
            BigInteger modulus = (BigInteger) objectInputStream.readObject();
            BigInteger exponent = (BigInteger) objectInputStream.readObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // TODO: Generate public or private
            Key key = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
            return key;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } finally {
            objectInputStream.close();
        }

        return null;
    }

    private String innerEncrypt(String plainMessage, File publicKeyfile) {
        try {
            Key publicKey = readKeyFromFile(publicKeyfile);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return new String(cipher.doFinal(plainMessage.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String innerDecrypt(String encryptedMessage, File privateKeyfile) {
        try {
            Key privateKey = readKeyFromFile(privateKeyfile);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return new String(cipher.doFinal(encryptedMessage.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
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

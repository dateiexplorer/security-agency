import java.io.File;

public interface IRSAAlgorithm {

    String encrypt(String plainMessage, File publicKeyfile);

    String decrypt(String encryptedMessage, File privateKeyfile);
}

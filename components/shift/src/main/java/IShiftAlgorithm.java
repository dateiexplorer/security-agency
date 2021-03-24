import java.io.File;

public interface IShiftAlgorithm {

    String encrypt(String plainMessage, File publicKeyfile);

    String decrypt(String encryptedMessage, File privateKeyfile);
}

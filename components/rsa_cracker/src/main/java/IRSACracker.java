import java.io.File;

public interface IRSACracker {

    String decrypt(String encryptedMessage, File keyfile);
}
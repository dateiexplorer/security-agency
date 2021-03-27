package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.FXMLController;
import de.dhbw.mosbach.msa.components.JarLoader;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class EncryptMessageCommand implements ICQLCommand {

    private final String message;
    private final String algorithm;
    private final String keyfile;

    public EncryptMessageCommand(String message, String algorithm, String keyfile) {
        this.message = message;
        this.algorithm = algorithm;
        this.keyfile = keyfile;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        FXMLController.logger.createNewLogfile("encrypt", algorithm);
        FXMLController.logger.info("execute EncryptMessageCommand");

        // Check if the algorithm exists.
        if (Configuration.instance.components.get(algorithm) == null) {
            FXMLController.logger.error("algorithm " + algorithm + " is not available");
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("algorithm %s is not available", algorithm)));
            return;
        }

        // Check if keyfile exists.
        File file = new File(Configuration.instance.keyDirectory + keyfile);
        if (!file.exists()) {
            FXMLController.logger.error("keyfile " + keyfile + " does not exists");
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("keyfile %s does not exist", keyfile)));
            return;
        }

        Object port = JarLoader.build(algorithm);
        try {
            FXMLController.logger.info("initialize encrypt method");
            Method method = port.getClass().getDeclaredMethod("encrypt", String.class, File.class);
            FXMLController.logger.info("invoke encrypt method to encrypt '" + message + "'");
            String encryptedMessage = (String) method.invoke(port, message, file);

            FXMLController.logger.info("message '" + message + "' encrypted as '" + encryptedMessage + "'");
            interpreter.result(new CQLResult(CQLResult.Type.OK, encryptedMessage));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            FXMLController.logger.error(e.getMessage());
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    "something went wrong - cannot encrypt message"));
        }

        FXMLController.logger.end("leave EncryptedMessageCommand");
    }
}

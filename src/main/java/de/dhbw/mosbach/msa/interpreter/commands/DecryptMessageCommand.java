package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.factory.Factory;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class DecryptMessageCommand implements ICQLCommand {

    private final String message;
    private final String algorithm;
    private final String keyfile;

    public DecryptMessageCommand(String message, String algorithm, String keyfile) {
        this.message = message;
        this.algorithm = algorithm;
        this.keyfile = keyfile;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        // Check if the algorithm exists.
        if (Configuration.instance.components.get(algorithm) == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("algorithm %s is not available.", algorithm)));
            return;
        }

        // Check if keyfile exists.
        URL fileURL = getClass().getClassLoader().getResource(Configuration.instance.keyDirectory + keyfile);
        if (fileURL == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("keyfile %s does not exist.", keyfile)));
            return;
        }

        Object port = Factory.build(algorithm);
        try {
            Method method = port.getClass().getDeclaredMethod("decrypt", String.class, File.class);
            String decryptedMessage = (String) method.invoke(port, message, new File(fileURL.getFile()));

            interpreter.result(new CQLResult(CQLResult.Type.OK, decryptedMessage));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    "something went wrong - cannot encrypt message"));
        }
    }
}
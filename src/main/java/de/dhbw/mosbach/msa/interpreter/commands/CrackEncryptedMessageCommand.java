package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.CrackService;
import de.dhbw.mosbach.msa.network.events.ResultEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;

public class CrackEncryptedMessageCommand implements ICQLCommand {

    private final String message;
    private final String algorithm;
    private final String keyfile;

    public CrackEncryptedMessageCommand(String message, String algorithm, String keyfile) {
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
        File file = null;
        if (keyfile != null) {
            URL fileURL = getClass().getClassLoader().getResource(Configuration.instance.keyDirectory + keyfile);
            if (fileURL == null) {
                interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                        String.format("keyfile %s does not exist.", keyfile)));
                return;
            } else {
                file = new File(fileURL.getFile());
            }
        }

        CrackService service = new CrackService(message, algorithm, file);
        service.setOnCancelled(event -> interpreter.result(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("cracking encrypted message \"%s\" failed", message))));

        service.setOnSucceeded(event -> {
            if (service.getValue() != null) {
                interpreter.result(new CQLResult(CQLResult.Type.MESSAGE, service.getValue()));
            } else {
                // If the result is null, message couldn't been cracked.
                interpreter.result(new CQLResult(CQLResult.Type.MESSAGE,
                        String.format("cracking encrypted message \"%s\" failed", message)));
            }
        });

        // Wait 30 seconds until break up the crack service.
        PauseTransition delay = new PauseTransition(Duration.seconds(30));
        delay.setOnFinished(event -> service.cancel());

        interpreter.result(new CQLResult(CQLResult.Type.OK,
                "try to crack message..."));
        service.start();
        delay.play();
    }
}

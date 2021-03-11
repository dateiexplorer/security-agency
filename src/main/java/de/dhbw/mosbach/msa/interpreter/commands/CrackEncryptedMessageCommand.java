package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;

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
        interpreter.result(new CQLResult(CQLResult.Type.OK,
                "msg: " + message + ", algorithm: " + algorithm + ", keyfile: " + keyfile));
    }
}

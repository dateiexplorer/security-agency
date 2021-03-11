package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;

public class NoCommandFound implements ICQLCommand {

    @Override
    public void execute(CQLInterpreter interpreter) {
        // Print a message for invalid syntax.
        interpreter.result(new CQLResult(CQLResult.Type.ERROR, "invalid syntax - no command found"));
    }
}

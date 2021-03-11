package de.dhbw.mosbach.msa.interpreter;

import de.dhbw.mosbach.msa.interpreter.commands.ICQLCommand;
import de.dhbw.mosbach.msa.network.Network;

import java.util.ArrayList;
import java.util.List;

public class CQLInterpreter {

    private ICQLCommand command;
    private final CQLParser parser;

    private List<ICQLInterpreterListener> listeners;

    public void setCommand(ICQLCommand command) {
        this.command = command;
    }

    public CQLInterpreter(Network network) {
        listeners = new ArrayList<>();
        parser = new CQLParser(network);
    }

    public void addListener(ICQLInterpreterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ICQLInterpreterListener listener) {
        listeners.add(listener);
    }

    public void execute(String query) {
        // Parse the command from the query.
        setCommand(parser.parse(query));

        // Execute this command with the interpreter.
        command.execute(this);
    }

    // Apply a new result.
    public void result(CQLResult result) {
        for (ICQLInterpreterListener listener : listeners) {
            listener.onResultReceived(result);
        }
    }
}

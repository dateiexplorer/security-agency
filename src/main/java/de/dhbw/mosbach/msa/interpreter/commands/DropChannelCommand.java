package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;

public class DropChannelCommand implements ICQLCommand {

    private final Network network;
    private final String name;

    public DropChannelCommand(Network network, String name) {
        this.network = network;
        this.name = name;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        // Check if channel exists.
        Channel channel = network.getChannel(name);
        if (channel == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("unknown channel %s", name)));
            return;
        }

        // Drop channel
        network.getChannels().remove(name);
        // TODO: Drop channel from database.

        interpreter.result(new CQLResult(CQLResult.Type.OK,
                String.format("channel %s deleted", name)));
    }
}

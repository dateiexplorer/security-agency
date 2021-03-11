package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

public class CreateChannelCommand implements ICQLCommand {

    private final Network network;
    private final String name;
    private final String participantFrom;
    private final String participantTo;

    public CreateChannelCommand(Network network, String name, String participantFrom, String participantTo) {
        this.network = network;
        this.name = name;
        this.participantFrom = participantFrom;
        this.participantTo = participantTo;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {

        // Check if channel already exists.
        if (network.getChannel(name) != null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("channel %s already exists", name)));
            return;
        }

        Participant from = network.getParticipant(participantFrom);
        Participant to = network.getParticipant(participantTo);

        // Check if participants exists.
        if (from == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("participant %s does not exists – cannot create channel",
                            participantFrom)));
            return;
        }

        if (to == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("participant %s does not exists – cannot create channel",
                            participantTo)));
            return;
        }

        // Check if channel with this participants exists.
        if (network.getChannelForParticipants(from, to) != null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("communication channel between %s and %s already exists",
                            participantFrom, participantTo)));
            return;
        }

        // Check if participants are identical.
        if (from == to) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("%s and %s are identical – cannot create channel on itself",
                            participantFrom, participantTo)));
            return;
        }

        // Create new channel.
        network.addChannel(
                new Channel(network, name, from, to));
        // TODO: Add database entry for channel.

        interpreter.result(new CQLResult(CQLResult.Type.OK, String.format("channel %s from %s to %s created",
                name, participantFrom, participantTo)));
    }
}

package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

public class IntrudeChannelCommand implements ICQLCommand {

    private Network network;
    private String name;
    private String participant;

    public IntrudeChannelCommand(Network network, String name, String participant) {
        this.network = network;
        this.name = name;
        this.participant = participant;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        // Check if channel exists
        Channel channel = network.getChannel(name);

        if (channel == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("channel %s does not exists - cannot intrude channel", name)));
            return;
        }

        // Check if participant exists
        Participant intruder = network.getParticipant(participant);

        if (intruder == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("participant %s does not exists - cannot intrude channel", participant)));
            return;
        }

        // Channel and Participant exists

        // Check if participant is an intruder
        if (intruder.getType() != Participant.Type.INTRUDER) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("participant %s is no intruder - cannot intrude channel", participant)));
            return;
        }

        channel.register(intruder);
        interpreter.result(new CQLResult(CQLResult.Type.OK,
                String.format("intruder %s is registered in channel %s", participant, name)));
    }
}

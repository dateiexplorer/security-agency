package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

public class SendMessageCommand implements ICQLCommand {

    private final Network network;
    private final String message;
    private final String participantFrom;
    private final String participantTo;
    private final String algorithm;
    private final String keyfile;

    public SendMessageCommand(Network network, String message, String participantFrom, String participantTo,
            String algorithm, String keyfile) {
        this.network = network;
        this.message = message;
        this.participantFrom = participantFrom;
        this.participantTo = participantTo;
        this.algorithm = algorithm;
        this.keyfile = keyfile;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        // Could be null.
        Participant from = network.getParticipant(participantFrom);
        Participant to = network.getParticipant(participantTo);

        Channel channel = network.getChannelForParticipants(from, to);

        if (channel == null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("no valid channel from %s to %s", participantFrom, participantTo)));
            return;
        }

        // Channel and participants exists.
        // Send message after setting the result, because the send method will causes into output.
        interpreter.result(new CQLResult(CQLResult.Type.OK,
                String.format("send message from %s to %s", participantFrom, participantTo)));

        // Do sending message through the channel.
        from.send(message, algorithm, keyfile, to, channel);
    }
}

package de.dhbw.mosbach.msa.interpreter.commands;

import java.util.List;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

public class ShowChannelCommand implements ICQLCommand {

    private Network network;

    public ShowChannelCommand(Network network) {
        this.network = network;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {
        StringBuilder message = new StringBuilder();
        for (Channel c : network.getChannels().values()) {
            List<Participant> participants = c.getParticipants();
            message.append(c.getName()).append(" | ").append(participants.get(0).getName())
                    .append(" and ").append(participants.get(1).getName());
        }

        interpreter.result(new CQLResult(CQLResult.Type.OK, message.toString()));
    }
}

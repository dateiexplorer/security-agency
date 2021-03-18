package de.dhbw.mosbach.msa.interpreter.commands;

import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

public class RegisterParticipantCommand implements ICQLCommand {

    private final Network network;
    private final String name;
    private final String type;

    public RegisterParticipantCommand(Network network, String name, String type) {
        this.network = network;
        this.name = name;
        this.type = type;
    }

    @Override
    public void execute(CQLInterpreter interpreter) {

        // Check if participant already exists.
        if (network.getParticipant(name) != null) {
            interpreter.result(new CQLResult(CQLResult.Type.ERROR,
                    String.format("participant %s already exists, using existing postbox_%s", name, name)));
            return;
        }

        Participant.Type type;

        // Check if type exists.
        try {
            type = Participant.Type.valueOf(this.type.toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder message = new StringBuilder(String.format("type %s is unknown - " +
                    "participant not registered - cannot create participant", this.type) + "\n\n" +
                    "Choose one from the available types below:\n");
            for (Participant.Type t : Participant.Type.values()) {
                message.append("  ").append(t.name().toUpperCase()).append("\n");
            }

            interpreter.result(new CQLResult(CQLResult.Type.ERROR, message.toString()));
            return;
        }

        // Create new participant.
        network.addParticipant(new Participant(name, type));

        interpreter.result(new CQLResult(CQLResult.Type.OK,
                String.format("participant %s with type %s registered and postbox_%s created",
                        name, type.name(), name)));
    }
}

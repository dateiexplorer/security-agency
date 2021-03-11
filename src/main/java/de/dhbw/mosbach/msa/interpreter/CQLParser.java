package de.dhbw.mosbach.msa.interpreter;

import de.dhbw.mosbach.msa.interpreter.commands.*;
import de.dhbw.mosbach.msa.network.Network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CQLParser {

    private final Network network;

    public CQLParser(Network network) {
        this.network = network;
    }

    public ICQLCommand parse(String query) {
        // Extract message from query.
        String message = null;
        Pattern messagePattern = Pattern.compile("\".*\"");

        Matcher messageMatcher = messagePattern.matcher(query);
        if (messageMatcher.find()) {
            message = messageMatcher.group();
            query = query.replaceAll(messagePattern.pattern(), "msg");
        }

        query = query.trim();

        // Split query in tokens (message is represented as 'msg').
        String[] tokens = query.split("\\s+");

        // --- Match query syntax.

        // Encrypt message
        Matcher encrypt = Pattern.compile(
                "(?i)encrypt\\s+message\\s+msg\\s+using\\s+[^\s]+\\s+and\\s+keyfile\\s+[^\s]+"
        ).matcher(query);

        if (encrypt.matches()) {
            return new EncryptMessageCommand(message, tokens[4], tokens[7]);
        }

        // Crack message
        Matcher crack = Pattern.compile(
                "(?i)crack\\s+encrypted\\s+message\\s+msg\\s+using\\s+[^\s]+(\\s+and\\s+keyfile\\s+[^\s]+)?"
        ).matcher(query);

        if (crack.matches()) {
            String keyfile = null;
            if (tokens.length == 9) {
                keyfile = tokens[8];
            }

            return new CrackEncryptedMessageCommand(message, tokens[5], keyfile);
        }

        // Register participant
        Matcher register = Pattern.compile(
                "(?i)register\\s+participant\\s+[^\s]+\\s+with\\s+type\\s+[^\s]+"
        ).matcher(query);

        if (register.matches()) {
            return new RegisterParticipantCommand(network, tokens[2], tokens[5]);
        }

        // Create channel
        Matcher create = Pattern.compile(
                "(?i)create\\s+channel\\s+[^\s]+\\s+from\\s+[^\s]+\\s+to\\s+[^\s]+"
        ).matcher(query);

        if (create.matches()) {
            return new CreateChannelCommand(network, tokens[2], tokens[4], tokens[6]);
        }

        // Show channels
        Matcher show = Pattern.compile(
                "(?i)show\\s+channel"
        ).matcher(query);

        if (show.matches()) {
            return new ShowChannelCommand(network);
        }

        // Drop channel
        Matcher drop = Pattern.compile(
                "(?i)drop\\s+channel\\s+[^\s]+"
        ).matcher(query);

        if (drop.matches()) {
            return new DropChannelCommand(network, tokens[2]);
        }

        // Intrude channel
        Matcher intrude = Pattern.compile(
                "(?i)intrude\\s+channel\\s+[^\s]+\\s+by\\s+[^\s]+"
        ).matcher(query);

        if (intrude.matches()) {
            return new IntrudeChannelCommand(network, tokens[2], tokens[4]);
        }

        // Send message
        Matcher send = Pattern.compile(
                "(?i)send\\s+message\\s+msg\\s+from\\s+[^\s]+\\s+to\\s+[^\s]+\\s+using\\s+[^\s]+\\s+" +
                        "and\\s+keyfile\\s[^\s]+"
        ).matcher(query);

        if (send.matches()) {
            return new SendMessageCommand(network, message, tokens[4], tokens[6], tokens[8], tokens[11]);
        }

        return new NoCommandFound();
    }


}

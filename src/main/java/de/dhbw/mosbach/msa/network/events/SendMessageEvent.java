package de.dhbw.mosbach.msa.network.events;

import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Participant;

import java.io.File;

public class SendMessageEvent {

    private final Participant from;
    private final Participant to;
    private final String encryptedMessage;
    private final String algorithm;
    private final File keyfile;
    private final Channel channel;

    public SendMessageEvent(Participant from, Participant to, String encryptedMessage, String algorithm,
                            File keyfile, Channel channel) {
        this.from = from;
        this.to = to;
        this.encryptedMessage = encryptedMessage;
        this.algorithm = algorithm;
        this.keyfile = keyfile;
        this.channel = channel;
    }

    public Participant getFrom() {
        return from;
    }

    public Participant getTo() {
        return to;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public File getKeyfile() {
        return keyfile;
    }

    public Channel getChannel() {
        return channel;
    }
}

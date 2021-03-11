package de.dhbw.mosbach.msa.network.events;

import de.dhbw.mosbach.msa.network.Channel;

public class SendMessageEvent {

    private final String from;
    private final String encryptedMessage;
    private final String algorithm;
    private final String keyfile;
    private final Channel channel;

    public SendMessageEvent(String from, String encryptedMessage, String algorithm, String keyfile, Channel channel) {
        this.from = from;
        this.encryptedMessage = encryptedMessage;
        this.algorithm = algorithm;
        this.keyfile = keyfile;
        this.channel = channel;
    }

    public String getFrom() {
        return from;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getKeyfile() {
        return keyfile;
    }

    public Channel getChannel() {
        return channel;
    }
}

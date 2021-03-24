package de.dhbw.mosbach.msa.network;

import com.google.common.eventbus.Subscribe;
import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.events.ResultEvent;
import de.dhbw.mosbach.msa.network.events.SendMessageEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Participant {

    private static final String MESSAGE_UNKNOWN = "unknown";

    public enum Type {
        NORMAL, INTRUDER
    }

    protected final String name;
    protected final Type type;

    public Participant(String name, Participant.Type type) {
        this.name = name;
        this.type = type;

        HSQLDB.instance.addParticipantToDatabase(this);
    }

    @Subscribe
    public void receive(SendMessageEvent event) {
        // Don't receive an event from yourself.
        if (this != event.getFrom()) {
            switch (type) {
                case NORMAL -> receive(event.getEncryptedMessage(), event.getAlgorithm(), event.getKeyfile(),
                        event.getFrom(), event.getChannel());
                case INTRUDER -> intrude(event.getEncryptedMessage(), event.getAlgorithm(), event.getFrom(),
                        event.getChannel());
            }
        }
    }

    public void send(String message, String algorithm, String keyfile, Participant to, Channel channel) {
        // TODO: Encrypt
        HSQLDB.instance.addMessageToDatabase(this, to, message, algorithm, message, keyfile);
        channel.postOnChannel(new SendMessageEvent(this, to, message, algorithm, keyfile, channel));
    }

    public void receive(String message, String algorithm, String keyfile, Participant from, Channel channel) {
        // TODO: Decrypt encrypt message
        HSQLDB.instance.addPostboxEntryToDatabase(this, from, message);
        channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("%s received new message", name))));
    }

    public void intrude(String message, String algorithm, Participant from, Channel channel) {
        // TODO: Crack encrypted message
        HSQLDB.instance.addPostboxEntryToDatabase(this, from, MESSAGE_UNKNOWN);

        IntrudeService service = new IntrudeService(message, algorithm);
        service.setOnCancelled(event -> channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("intruder %s | crack message from participant %s failed", name, from)))));

        service.setOnSucceeded(event -> channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("intruder %s cracked message from participant %s | %s",
                        name, from, service.getValue())))));

        service.start();

        PauseTransition delay = new PauseTransition(Duration.seconds(30));
        delay.play();
        delay.setOnFinished(event -> service.cancel());
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}

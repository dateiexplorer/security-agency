package de.dhbw.mosbach.msa.network;

import com.google.common.eventbus.Subscribe;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.events.ResultEvent;
import de.dhbw.mosbach.msa.network.events.SendMessageEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Participant {

    public enum Type {
        NORMAL, INTRUDER
    }

    protected final String name;
    protected final Type type;

    public Participant(String name, Participant.Type type) {
        this.name = name;
        this.type = type;
    }

    @Subscribe
    public void receive(SendMessageEvent event) {
        // Don't receive an event from yourself.
        if (!name.equals(event.getFrom())) {
            switch (type) {
                case NORMAL -> receive(event.getEncryptedMessage(), event.getAlgorithm(), event.getKeyfile(),
                        event.getChannel());
                case INTRUDER -> intrude(event.getEncryptedMessage(), event.getAlgorithm(), event.getFrom(),
                        event.getChannel());
            }
        }
    }

    public void send(String message, String algorithm, String keyfile, Channel channel) {
        channel.postOnChannel(new SendMessageEvent(name, message, algorithm, keyfile, channel));
    }

    public void receive(String message, String algorithm, String keyfile, Channel channel) {
        // TODO: Decrypt encrypt message
        channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("%s received new message", name))));
    }

    public void intrude(String message, String algorithm, String from, Channel channel) {
        // TODO: Crack encrypted message
        IntrudeService service = new IntrudeService(message, algorithm);
        service.setOnCancelled(event -> {
            channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                    String.format("intruder %s | crack message from participant %s failed", name, from))));
        });

        service.setOnSucceeded(event -> {
            channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                    String.format("intruder %s cracked message from participant %s | %s",
                            name, from, service.getValue()))));
        });

        service.start();

        PauseTransition delay = new PauseTransition(Duration.seconds(30));
        delay.play();
        delay.setOnFinished(event -> {
            service.cancel();
        });
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}

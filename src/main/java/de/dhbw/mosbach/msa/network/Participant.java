package de.dhbw.mosbach.msa.network;

import com.google.common.eventbus.Subscribe;
import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.components.JarLoader;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.network.events.ResultEvent;
import de.dhbw.mosbach.msa.network.events.SendMessageEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
                case INTRUDER -> intrude(event.getEncryptedMessage(), event.getAlgorithm(), event.getKeyfile(),
                        event.getFrom(), event.getChannel());
            }
        }
    }

    public void send(String message, String algorithm, File keyfile, Participant to, Channel channel) {
        Object port = JarLoader.build(algorithm);
        try {
            Method method = port.getClass().getDeclaredMethod("encrypt", String.class, File.class);
            String encryptedMessage = (String) method.invoke(port, message, keyfile);

            HSQLDB.instance.addMessageToDatabase(this, to, message, algorithm, encryptedMessage,
                    keyfile.getName());

            channel.postOnChannel(new SendMessageEvent(this, to, encryptedMessage, algorithm, keyfile, channel));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void receive(String message, String algorithm, File keyfile, Participant from, Channel channel) {
        Object port = JarLoader.build(algorithm);

        try {
            Method method = port.getClass().getDeclaredMethod("decrypt", String.class, File.class);
            String decryptedMessage = (String) method.invoke(port, message, keyfile);

            HSQLDB.instance.addPostboxEntryToDatabase(this, from, decryptedMessage);
            channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                    String.format("%s received new message", name))));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void intrude(String message, String algorithm, File keyfile, Participant from, Channel channel) {
        int messageId = HSQLDB.instance.addPostboxEntryToDatabase(this, from, MESSAGE_UNKNOWN);

        CrackService service = new CrackService(message, algorithm, keyfile);
        service.setOnCancelled(event -> channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                String.format("intruder %s | crack message from participant %s failed", name, from.getName())))));

        service.setOnSucceeded(event -> {
            if (service.getValue() != null) {
                HSQLDB.instance.updatePostboxEntryInDatabase(messageId, this, service.getValue());
                channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                        String.format("intruder %s cracked message from participant %s | %s",
                                name, from.getName(), service.getValue()))));
            } else {
                // If the result is null, message couldn't been cracked.
                channel.postOnNetwork(new ResultEvent(new CQLResult(CQLResult.Type.MESSAGE,
                        String.format("intruder %s | crack message from participant %s failed",
                                name, from.getName()))));
            }
        });

        service.start();

        // Wait 30 seconds until break up the crack service.
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

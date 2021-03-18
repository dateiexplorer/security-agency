package de.dhbw.mosbach.msa.network;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.network.events.ResultEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Network {

    private final Map<String, Participant> participants;
    private final Map<String, Channel> channels;

    private final List<INetworkListener> listeners;

    private EventBus eventBus;

    public Network() {
        listeners = new ArrayList<>();

        participants = new HashMap<>();
        channels = new HashMap<>();

        eventBus = new EventBus("network");
        eventBus.register(this);

        // Load existing data from database into network.
        HSQLDB.instance.addParticipantsToNetwork(this);
        HSQLDB.instance.addChannelsToNetwork(this);
    }

    public void addListener(INetworkListener listener) {
        listeners.add(listener);
    }

    public void removeListener(INetworkListener listener) {
        listeners.remove(listener);
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    @Subscribe
    public void receive(ResultEvent event) {
        for (INetworkListener listener : listeners) {
            listener.onMessageReceived(event);
        }
    }

    public void addParticipant(Participant participant) {
        participants.put(participant.getName(), participant);
    }

    public void addChannel(Channel channel) {
        channels.put(channel.getName(), channel);
    }

    public void removeChannelByName(String name) {
        channels.remove(name);
        HSQLDB.instance.removeChannelFromDatabase(name);
    }

    public Participant getParticipant(String name) {
        return participants.get(name);
    }

    public Channel getChannel(String name) {
        return channels.get(name);
    }

    public Channel getChannelForParticipants(Participant participantFrom, Participant participantTo) {
        List<Participant> list = new ArrayList<>();
        list.add(participantFrom);
        list.add(participantTo);

        for (Channel c : channels.values()) {
            if (c.getParticipants().containsAll(list)) {
                return c;
            }
        }

        return null;
    }

    public Map<String, Participant> getParticipants() {
        return participants;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }
}

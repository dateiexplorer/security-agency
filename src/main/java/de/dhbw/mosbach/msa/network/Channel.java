package de.dhbw.mosbach.msa.network;

import com.google.common.eventbus.EventBus;
import de.dhbw.mosbach.msa.database.HSQLDB;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    private final EventBus eventBus;

    private final String name;
    private final Participant from;
    private final Participant to;
    private final Network network;

    public Channel(Network network, String name, Participant from, Participant to) {
        this.network = network;
        this.name = name;
        this.from = from;
        this.to = to;

        eventBus = new EventBus(name);
        register(from);
        register(to);

        HSQLDB.instance.addChannelToDatabase(this);
    }

    public void register(Participant participant) {
        eventBus.register(participant);
    }

    public void unregister(Participant participant) {
        eventBus.unregister(participant);
    }

    public void postOnChannel(Object event) {
        eventBus.post(event);
    }

    public void postOnNetwork(Object event) {
        network.post(event);
    }

    public List<Participant> getParticipants() {
        List<Participant> list = new ArrayList<>();
        list.add(from);
        list.add(to);
        return list;
    }

    public String getName() {
        return name;
    }
}

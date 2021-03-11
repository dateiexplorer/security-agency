package de.dhbw.mosbach.msa.network;

import de.dhbw.mosbach.msa.network.events.ResultEvent;

public interface INetworkListener {

    void onMessageReceived(ResultEvent event);
}

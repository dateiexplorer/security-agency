package de.dhbw.mosbach.msa.network.events;

import de.dhbw.mosbach.msa.interpreter.CQLResult;

public class ResultEvent {

    private final CQLResult result;

    public ResultEvent(CQLResult result) {
        this.result = result;
    }

    public CQLResult getResult() {
        return result;
    }
}

package de.dhbw.mosbach.msa.network;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class IntrudeService extends Service<String> {

    private final String message;
    private final String algorithm;

    public IntrudeService(String message, String algorithm) {
        this.message = message;
        this.algorithm = algorithm;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {

            @Override
            protected String call() throws InterruptedException {
                return message;
            }
        };
    }
}

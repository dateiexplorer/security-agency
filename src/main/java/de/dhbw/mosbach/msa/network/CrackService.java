package de.dhbw.mosbach.msa.network;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.components.JarLoader;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CrackService extends Service<String> {

    private final String message;
    private final String cracker;
    private final File keyfile;

    public CrackService(String message, String algorithm, File keyfile) {
        this.message = message;
        this.cracker = algorithm + "_cracker";
        this.keyfile = keyfile;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {

            @Override
            protected String call() throws InterruptedException {
                // Check if a cracker for this algorithm exists.
                if (Configuration.instance.components.get(cracker) == null) {
                    return null;
                }

                Object port = JarLoader.build(cracker);

                try {
                    Method method = null;

                    String[] tokens = Configuration.instance.components.get(cracker);

                    // Do this cracker need a keyfile?
                    if (tokens.length >= 3 && tokens[2].equals("no_keyfile")) {
                        method = port.getClass().getDeclaredMethod("decrypt", String.class);
                        return (String) method.invoke(port, message);
                    } else {
                        method = port.getClass().getDeclaredMethod("decrypt", String.class, File.class);
                        return (String) method.invoke(port, message, keyfile);
                    }

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }
}

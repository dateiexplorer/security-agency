package de.dhbw.mosbach.msa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum Configuration {
    instance;

    private final String fileSeparator = System.getProperty("file.separator");

    final String userDirectory = System.getProperty("user.dir");
    final String dataDirectory = userDirectory + fileSeparator + "data" + fileSeparator;

    // Jarsigner
    public final String jarsigner = System.getProperty("java.home") + fileSeparator + "bin" + fileSeparator +
            "jarsigner";

    // Database connection
    public final String driverName = "jdbc:hsqldb:";
    public final String username = "msa";
    public final String password = "";
    public final String databaseFile = dataDirectory + "datastore.db";

    public final String keyDirectory = "keys" + fileSeparator;
    public final String logDirectory = "logs" + fileSeparator;

    // Components
    public Map<String, String[]> components = loadComponentsFromFile("components.csv");

    private Map<String, String[]> loadComponentsFromFile(String file) {
        Map<String, String[]> components = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while (null != (line = reader.readLine())) {
                String[] tokens = line.split(";");

                if (tokens.length >= 4) {
                    components.put(tokens[0], new String[] { tokens[1], userDirectory + tokens[2], tokens[3] });
                } else {
                    components.put(tokens[0], new String[] { tokens[1], userDirectory + tokens[2] });
                }
            }

            return components;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

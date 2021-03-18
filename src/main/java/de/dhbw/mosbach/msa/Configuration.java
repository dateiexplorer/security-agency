package de.dhbw.mosbach.msa;

public enum Configuration {
    instance;

    String userDirectory = System.getProperty("user.dir");
    String fileSeparator = System.getProperty("file.separator");
    String dataDirectory = userDirectory + fileSeparator + "data" + fileSeparator;

    // Database connection
    public final String driverName = "jdbc:hsqldb:";
    public final String username = "msa";
    public final String password = "";
    public final String databaseFile = dataDirectory + "datastore.db";
}

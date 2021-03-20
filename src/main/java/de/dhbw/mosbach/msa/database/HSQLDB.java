package de.dhbw.mosbach.msa.database;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.network.Channel;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.Participant;

import java.sql.*;

public enum HSQLDB {
    instance;

    private Connection connection;

    public void setupConnection() {
        System.out.println("--- setupConnection");

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            String databaseURL = Configuration.instance.driverName + Configuration.instance.databaseFile;
            connection = DriverManager.getConnection(databaseURL, Configuration.instance.username,
                    Configuration.instance.password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private synchronized void update(String sqlStatement) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlStatement);
            statement.close();
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }
    }

    private synchronized ResultSet query(String sqlStatement) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);
            statement.close();
            return result;
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }

        return null;
    }

    private int getIdForParticipant(Participant participant) {
        String sqlStringBuilder01 = "SELECT id FROM participants WHERE name = '" +
                participant.getName() + "'";
        ResultSet result = query(sqlStringBuilder01);
        try {
            assert result != null;
            result.next();
            return result.getInt("id");
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }

        return -1;
    }

    private int getIdForAlgorithm(String algorithm) {
        String sqlStringBuilder01 = "SELECT id FROM algorithms WHERE name = '" +
                algorithm + "'";
        ResultSet result = query(sqlStringBuilder01);
        try {
            assert result != null;
            result.next();
            return result.getInt("id");
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }

        return -1;
    }

    public void addParticipantsToNetwork(Network network) {
        String sqlStringBuilder01 = "SELECT participants.name AS id, types.name AS type" + " " +
                "FROM participants" + " " +
                "INNER JOIN types ON participants.type_id = types.id";
        ResultSet results = query(sqlStringBuilder01);

        try {
            while (results.next()) {
                String id = results.getString("id");
                Participant.Type type = Participant.Type.valueOf(results.getString("type"));

                network.addParticipant(new Participant(id, type));
            }

        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }
    }

    public void addChannelsToNetwork(Network network) {
        String sqlStringBuilder01 = "SELECT channel.name AS name" + "," +
                "p_from.name AS from_id, p_to.name AS to_id" + " " +
                "FROM channel" + " " +
                "INNER JOIN participants p_from ON channel.participant_01 = p_from.id" + " " +
                "INNER JOIN participants p_to ON channel.participant_02 = p_to.id" + " ";
        ResultSet results = query(sqlStringBuilder01);

        try {
            while (results.next()) {
                String name = results.getString("name");
                Participant from = network.getParticipant(results.getString("from_id"));
                Participant to = network.getParticipant(results.getString("to_id"));

                network.addChannel(new Channel(network, name, from, to));
            }
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }
    }

    public void addParticipantToDatabase(Participant participant) {
        String sqlStringBuilder01 = "SELECT name FROM participants WHERE name = '" + participant.getName() + "'";
        ResultSet result = query(sqlStringBuilder01);

        // Check if a participant with this name already exists.
        try {
            if (result.next()) {
                return;
            }
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }

        // Query to add the participant to the participants list.
        String sqlStringBuilder02 = "INSERT INTO participants (name, type_id) VALUES ('" +
                participant.getName() + "', " +
                participant.getType().ordinal() + ")";
        update(sqlStringBuilder02);

        // Create new table for this participant.
        createTablePostbox(participant.getName());
    }

    public void addChannelToDatabase(Channel channel) {
        // Query to add the channel to the channel list.
        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("INSERT INTO channel (name, participant_01, participant_02) VALUES ('");
        sqlStringBuilder01.append(channel.getName()).append("', ");

        int from = getIdForParticipant(channel.getParticipants().get(0));
        int to = getIdForParticipant(channel.getParticipants().get(1));

        sqlStringBuilder01.append(from).append(", ").append(to).append(")");
        update(sqlStringBuilder01.toString());
    }

    public void removeChannelFromDatabase(String name) {
        // Query to remove the channel from the channel list.
        update("DELETE FROM channel WHERE name = '" + name + "'");
    }

    public void addMessageToDatabase(Participant from, Participant to, String plainMessage, String algorithm,
            String encryptedMessage, String keyfile) {
        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("INSERT INTO messages (");
        sqlStringBuilder01.append("participant_from_id").append(",");
        sqlStringBuilder01.append("participant_to_id").append(",");
        sqlStringBuilder01.append("plain_message").append(",");
        sqlStringBuilder01.append("algorithm_id").append(",");
        sqlStringBuilder01.append("encrypted_message").append(",");
        sqlStringBuilder01.append("keyfile").append(",");
        sqlStringBuilder01.append("timestamp").append(") VALUES (");
        sqlStringBuilder01.append(getIdForParticipant(from)).append(",");
        sqlStringBuilder01.append(getIdForParticipant(to)).append(",");
        sqlStringBuilder01.append("'").append(plainMessage).append("',");
        sqlStringBuilder01.append(getIdForAlgorithm(algorithm)).append(",");
        sqlStringBuilder01.append("'").append(encryptedMessage).append("',");
        sqlStringBuilder01.append("'").append(keyfile).append("',");
        sqlStringBuilder01.append("UNIX_TIMESTAMP())");

        update(sqlStringBuilder01.toString());
    }

    // Returns the id of this entry to update it later by the intruder.
    public int addPostboxEntryToDatabase(Participant to, Participant from, String message) {
        String sqlStringBuilder01 = "INSERT INTO postbox_" + to.getName() + " (" +
                "participant_from_id" + "," +
                "message" + "," +
                "timestamp" + ") VALUES (" +
                getIdForParticipant(from) + "," +
                "'" + message + "'," +
                "UNIX_TIMESTAMP())";
        update(sqlStringBuilder01);

        ResultSet result = query("SELECT id FROM postbox_" + to.getName() + " ORDER BY timestamp DESC");
        try {
            result.next();
            return result.getInt("id");
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }

        return -1;
    }

    public void updatePostboxEntryInDatabase(int id, Participant to, String message) {
        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("UPDATE postbox_").append(to.getName()).append(" SET ");
        sqlStringBuilder01.append("message = '").append(message).append("',");
        sqlStringBuilder01.append("timestamp = UNIX_TIMESTAMP() ");
        sqlStringBuilder01.append("WHERE id = ").append(id);

        update(sqlStringBuilder01.toString());
    }

    public void addAlgorithmToDatabase(String algorithm) {
        String sqlStringBuilder01 = "INSERT INTO algorithms (name) VALUES ('" + algorithm + "')";
        update(sqlStringBuilder01);
    }

    /*
       [algorithms]
       id TINYINT       NOT NULL PK
       name VARCHAR(10) NOT NULL unique
    */
    public void createTableAlgorithms() {
        System.out.println("--- createTableAlgorithms");

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS algorithms (");
        sqlStringBuilder01.append("id TINYINT GENERATED BY DEFAULT AS IDENTITY").append(",");
        sqlStringBuilder01.append("name VARCHAR(10) NOT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (id)");
        sqlStringBuilder01.append(")");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("CREATE UNIQUE INDEX IF NOT EXISTS idx_algorithms ON algorithms (name)");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());

        // TODO: Load algorithms dynamically
        addAlgorithmToDatabase("rsa");
        addAlgorithmToDatabase("shift");
    }

    /*
       [types]
       id TINYINT       NOT NULL PK
       name VARCHAR(10) NOT NULL unique
    */
    public void createTableTypes() {
        System.out.println("--- createTableTypes");

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS types (");
        sqlStringBuilder01.append("id TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("name VARCHAR(10) NOT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (id)");
        sqlStringBuilder01.append(")");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("CREATE UNIQUE INDEX IF NOT EXISTS idx_types ON types (name)");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());

        for (int i = 0; i < Participant.Type.values().length; i++) {
            addTypeToDatabase(Participant.Type.values()[i]);
        }
    }

    private void addTypeToDatabase(Participant.Type type) {
        String sqlStringBuilder01 = "INSERT INTO types (id, name) VALUES (" +
                type.ordinal() + ", '" +
                type.name() + "')";
        update(sqlStringBuilder01);
    }

    /*
       [participants]
       id TINYINT       NOT NULL PK
       name VARCHAR(50) NOT NULL unique
       type_id TINYINT  NOT NULL FK
    */
    public void createTableParticipants() {
        System.out.println("--- createTableParticipants");

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS participants (");
        sqlStringBuilder01.append("id TINYINT GENERATED BY DEFAULT AS IDENTITY").append(",");
        sqlStringBuilder01.append("name VARCHAR(50) NOT NULL").append(",");
        sqlStringBuilder01.append("type_id TINYINT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (id)");
        sqlStringBuilder01.append(")");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("CREATE UNIQUE INDEX IF NOT EXISTS idx_participants ON types (name)");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());

        StringBuilder sqlStringBuilder03 = new StringBuilder();
        sqlStringBuilder03.append("ALTER TABLE participants ADD CONSTRAINT IF NOT EXISTS fk_participants ");
        sqlStringBuilder03.append("FOREIGN KEY (type_id) ");
        sqlStringBuilder03.append("REFERENCES types (id) ");
        sqlStringBuilder03.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder03.toString());

        update(sqlStringBuilder03.toString());
    }

    /*
       [channel]
       name           VARCHAR(25) NOT NULL PK
       participant_01 TINYINT NOT NULL FK
       participant_02 TINYINT NOT NULL FK
    */
    public void createTableChannel() {
        System.out.println("--- createTableChannel");

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS channel (");
        sqlStringBuilder01.append("name VARCHAR(25) NOT NULL").append(",");
        sqlStringBuilder01.append("participant_01 TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("participant_02 TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (name)");
        sqlStringBuilder01.append(" )");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("ALTER TABLE channel ADD CONSTRAINT IF NOT EXISTS fk_channel_01 ");
        sqlStringBuilder02.append("FOREIGN KEY (participant_01) ");
        sqlStringBuilder02.append("REFERENCES participants (id) ");
        sqlStringBuilder02.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());

        StringBuilder sqlStringBuilder03 = new StringBuilder();
        sqlStringBuilder03.append("ALTER TABLE channel ADD CONSTRAINT IF NOT EXISTS fk_channel_02 ");
        sqlStringBuilder03.append("FOREIGN KEY (participant_02) ");
        sqlStringBuilder03.append("REFERENCES participants (id) ");
        sqlStringBuilder03.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder03.toString());
        update(sqlStringBuilder03.toString());
    }

    /*
      [messages]
      id                  TINYINT NOT NULL
      participant_from_id TINYINT NOT NULL
      participant_to_id   TINYINT NOT NULL
      plain_message       VARCHAR(50) NOT NULL
      algorithm_id        TINYINT NOT NULL
      encrypted_message   VARCHAR(50) NOT NULL
      keyfile             VARCHAR(20) NOT NULL
      timestamp           INT
    */
    public void createTableMessages() {
        System.out.println("--- createTableMessages");

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS messages (");
        sqlStringBuilder01.append("id TINYINT GENERATED BY DEFAULT AS IDENTITY").append(",");
        sqlStringBuilder01.append("participant_from_id TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("participant_to_id TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("plain_message VARCHAR(50) NOT NULL").append(",");
        sqlStringBuilder01.append("algorithm_id TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("encrypted_message VARCHAR(50) NOT NULL").append(",");
        sqlStringBuilder01.append("keyfile VARCHAR(20) NOT NULL").append(",");
        sqlStringBuilder01.append("timestamp BIGINT NOT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (id)");
        sqlStringBuilder01.append(" )");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_01 ");
        sqlStringBuilder02.append("FOREIGN KEY (participant_from_id) ");
        sqlStringBuilder02.append("REFERENCES participants (id) ");
        sqlStringBuilder02.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());

        StringBuilder sqlStringBuilder03 = new StringBuilder();
        sqlStringBuilder03.append("ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_02 ");
        sqlStringBuilder03.append("FOREIGN KEY (participant_to_id) ");
        sqlStringBuilder03.append("REFERENCES participants (id) ");
        sqlStringBuilder03.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder03.toString());
        update(sqlStringBuilder03.toString());

        StringBuilder sqlStringBuilder04 = new StringBuilder();
        sqlStringBuilder04.append("ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_03 ");
        sqlStringBuilder04.append("FOREIGN KEY (algorithm_id) ");
        sqlStringBuilder04.append("REFERENCES algorithms (id) ");
        sqlStringBuilder04.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder04.toString());
        update(sqlStringBuilder04.toString());
    }

    /*
       [postbox_[participant_name]]
       id                  TINYINT NOT NULL
       participant_from_id TINYINT NOT NULL
       message             VARCHAR(50) NOT NULL
       timestamp           INT
     */
    public void createTablePostbox(String participantName) {
        String table = "postbox_" + participantName;
        System.out.println("--- createTablePostbox_" + participantName);

        StringBuilder sqlStringBuilder01 = new StringBuilder();
        sqlStringBuilder01.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");
        sqlStringBuilder01.append("id TINYINT GENERATED BY DEFAULT AS IDENTITY").append(",");
        sqlStringBuilder01.append("participant_from_id TINYINT NOT NULL").append(",");
        sqlStringBuilder01.append("message VARCHAR(50) NOT NULL").append(",");
        sqlStringBuilder01.append("timestamp BIGINT NOT NULL").append(",");
        sqlStringBuilder01.append("PRIMARY KEY (id)");
        sqlStringBuilder01.append(")");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder01.toString());
        update(sqlStringBuilder01.toString());

        StringBuilder sqlStringBuilder02 = new StringBuilder();
        sqlStringBuilder02.append("ALTER TABLE ").append(table).append(" ADD CONSTRAINT IF NOT EXISTS fk_postbox_")
                .append(participantName);
        sqlStringBuilder02.append(" FOREIGN KEY (participant_from_id) ");
        sqlStringBuilder02.append("REFERENCES participants (id) ");
        sqlStringBuilder02.append("ON DELETE CASCADE");
        System.out.println("sqlStringBuilder : " + sqlStringBuilder02.toString());
        update(sqlStringBuilder02.toString());
    }

    public void shutdown() {
        System.out.println("--- shutdown");

        try {
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            connection.close();
        } catch (SQLException sql) {
            System.out.println(sql.getMessage());
        }
    }

    public void setupDatabase() {
        setupConnection();
        createTableAlgorithms();
        createTableTypes();
        createTableParticipants();
        createTableChannel();
        createTableMessages();
    }
}
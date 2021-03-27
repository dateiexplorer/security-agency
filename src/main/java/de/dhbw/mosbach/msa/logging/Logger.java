package de.dhbw.mosbach.msa.logging;

import de.dhbw.mosbach.msa.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {

    private enum Type {
        INFO, ERROR
    }

    private File currentFile;
    private boolean active;

    private BufferedWriter writer;

    public void createNewLogfile(String action, String algorithm) {
        currentFile = new File(Configuration.instance.logDirectory +
                String.format("%s_%s_%d.txt", action, algorithm, getUnixTimestamp()));
    }

    public void end(String message) {
        info(message);
        currentFile = null;
    }

    public File getLastLogFile() {
        File lastLogFile = null;
        File[] files = new File(Configuration.instance.logDirectory).listFiles();

        long lastTimestamp = 0;
        for (File file : files) {
            Matcher timestampMatcher = Pattern.compile("([0-9]+)").matcher(file.getName());
            if (timestampMatcher.find()) {
                long timestamp = Long.parseLong(timestampMatcher.group());
                if (timestamp > lastTimestamp) {
                    lastLogFile = file;
                    lastTimestamp = timestamp;
                }
            }
        }

        return lastLogFile;
    }

    public String getContent(File file) {
        if (file == null) {
            return null;
        }

        try {
            StringBuilder content = new StringBuilder();
            Files.lines(file.toPath()).forEach(l -> content.append(l).append("\n"));
            return content.toString();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    private void log(Type type, String message) {
        if (!active || currentFile == null) {
            return;
        }

        try {
            if (!currentFile.exists()) {
                currentFile.createNewFile();
                writer = new BufferedWriter(new FileWriter(currentFile, true));
            }

            writer.write(new SimpleDateFormat("dd.MM HH:mm:ss.SSS")
                    .format(new Date(getUnixTimestamp())) + " | " + type.name() + " | " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void info(String message) {
        log(Type.INFO, message);
    }

    public void error(String message) {
        log(Type.ERROR, message);
    }

    public long getUnixTimestamp() {
        return System.currentTimeMillis();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}

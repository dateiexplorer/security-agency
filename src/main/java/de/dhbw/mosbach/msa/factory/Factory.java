package de.dhbw.mosbach.msa.factory;

import de.dhbw.mosbach.msa.Configuration;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

public class Factory {

    public static Object build(String component) {
        Object port = null;

        try {
            if (verifyJarArchive(Configuration.instance.components.get(component)[1])) {
                URL[] urls = { new File(Configuration.instance.components.get(component)[1]).toURI().toURL() };
                URLClassLoader urlClassLoader = new URLClassLoader(urls, Factory.class.getClassLoader());
                Class clazz = Class.forName(Configuration.instance.components.get(component)[0],
                        true, urlClassLoader);

                Object instance = clazz.getMethod("getInstance").invoke(null);

                port = clazz.getDeclaredField("port").get(instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return port;
    }

    private static boolean verifyJarArchive(String path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(Configuration.instance.jarsigner, "-verify", path);
        Process process = processBuilder.start();
        process.waitFor();

        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        while (null != (line = bufferedReader.readLine())) {
            if (line.contains("verified")) {
                return true;
            }
        }

        return false;
    }
}

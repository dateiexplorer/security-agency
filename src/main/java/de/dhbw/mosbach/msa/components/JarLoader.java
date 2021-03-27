package de.dhbw.mosbach.msa.components;

import de.dhbw.mosbach.msa.Configuration;
import de.dhbw.mosbach.msa.FXMLController;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader {

    public static Object build(String component) {
        FXMLController.logger.info("loading port for component " + component);
        Object port = null;

        try {
            File file = new File(Configuration.instance.components.get(component)[1]);
            if (verifyJarArchive(file.getAbsolutePath())) {
                FXMLController.logger.info("component " + component + " is signed");
                FXMLController.logger.info("load jar archive for component " + component);
                URL[] urls = { file.toURI().toURL() };

                URLClassLoader urlClassLoader = new URLClassLoader(urls, JarLoader.class.getClassLoader());
                Class clazz = Class.forName(Configuration.instance.components.get(component)[0],
                        true, urlClassLoader);

                Object instance = clazz.getMethod("getInstance").invoke(null);
                port = clazz.getDeclaredField("port").get(instance);
                FXMLController.logger.info("port loaded successfully for component " + component);
            } else {
                FXMLController.logger.info("component" + component + " is not signed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            FXMLController.logger.error(e.getMessage());
        }

        return port;
    }

    private static boolean verifyJarArchive(String path) throws IOException, InterruptedException {
        FXMLController.logger.info("verify jar archive");
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

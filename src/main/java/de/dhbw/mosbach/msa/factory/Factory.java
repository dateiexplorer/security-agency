package de.dhbw.mosbach.msa.factory;

import de.dhbw.mosbach.msa.Configuration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class Factory {

    public static Object build(String component) {
        Object port = null;

        try {
            URL[] urls = { new File(Configuration.instance.components.get(component)[1]).toURI().toURL() };
            URLClassLoader urlClassLoader = new URLClassLoader(urls, Factory.class.getClassLoader());
            Class clazz = Class.forName(Configuration.instance.components.get(component)[0],
                    true, urlClassLoader);

            Object instance = clazz.getMethod("getInstance").invoke(null);

            port = clazz.getDeclaredField("port").get(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return port;
    }
}

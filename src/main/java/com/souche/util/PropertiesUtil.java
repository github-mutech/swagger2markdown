package com.souche.util;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author H
 */
public class PropertiesUtil {

    private static PropertiesUtil instance;

    private static Properties properties;

    static {
        instance = new PropertiesUtil();
        properties = new Properties();

        InputStream in = null;
        try {
            String file = System.getProperty("user.dir")
                    .concat(File.separator)
                    .concat("src")
                    .concat(File.separator)
                    .concat("main")
                    .concat(File.separator)
                    .concat("resources")
                    .concat(File.separator)
                    .concat("application.properties");
            in = new FileInputStream(file);
            properties.load(new InputStreamReader(in, "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private PropertiesUtil() {
    }


    public static PropertiesUtil getInstance() {
        return instance;
    }

    public String getStringValue(String key) {
        return properties.getProperty(key);
    }

    public List<String> getListStringValue(String key) {
        String property = properties.getProperty(key);
        if (property.isEmpty()) {
            throw new IllegalArgumentException(key + "=");
        }
        return Arrays.asList(property.split(","));
    }

    public Integer getIntegerValue(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}

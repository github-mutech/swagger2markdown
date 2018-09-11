package com.souche.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        FileInputStream in = null;
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
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

    public Integer getIntegerValue(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}

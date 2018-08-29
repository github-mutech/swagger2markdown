package com.souche.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author huchao
 */
public class PropertiesUtil {

    private static PropertiesUtil instance;

    private static Properties properties;

    static {
        instance = new PropertiesUtil();
        properties = new Properties();
        FileInputStream in = null;
        try {
            String file = System.getProperty("user.dir") + "\\src\\main\\resource\\application.properties";
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

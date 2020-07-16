package org.telegram.expensesbot.factory;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesFactory {
    private static final Logger log = LoggerFactory.getLogger(PropertiesFactory.class);

    public static String getProperty(String fileName, String propertyName) {
        Properties prop = new Properties();
        try {
            prop.load(PropertiesFactory.class.getClassLoader().getResourceAsStream(fileName));

            return prop.getProperty(propertyName);
        } catch (IOException ex) {
            log.error("Cannot load property: {} from file: {}", propertyName, fileName, ex);
        }

        return "";
    }
}

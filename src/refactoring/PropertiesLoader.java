package Refactoring;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by pip on 15.02.2016.
 */
public class PropertiesLoader {

    Properties properties;

    public PropertiesLoader() {

        properties = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        try {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        }catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }

}

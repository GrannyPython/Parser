package ifmo.escience.newscrawler.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Alexander Visheratin
 */
public class ConfigReader {
    private static final String configFileName = "config.properties";
    private static Logger logger = LogManager.getLogger(ConfigReader.class.getName());

    public static String getProperty(String name) throws IOException {
        FileInputStream inputStream = null;
        String property = null;
        try {
            Properties properties = new Properties();
            inputStream = new FileInputStream(configFileName);
            properties.load(inputStream);
            property = properties.getProperty(name);
            return property;
        } catch (FileNotFoundException ex) {
            logger.error("Config file not found!", ex);
        } catch (IOException ex) {
            logger.error("Error on reading parameter!", ex);
        } finally {
            inputStream.close();
        }
        return property;
    }
}

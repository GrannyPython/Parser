package ifmo.escience.newscrawler;

import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.logging.Level;

public class Main {
    static {
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

    }

    private static Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            DBConnection.getDbConfigs();
            Crawler crawler = new Crawler();
            logger.trace("Crawler has started working");
            crawler.start();
        } catch (Exception ex) {
            logger.trace("Error on starting crawling", ex);
        }
//        String text = "";
//
//        text = text.replaceAll("[,.!\"«\']"," ").replaceAll("\n"," ");
//        String[] olo =  KeyWords.doIt(text);
    }
}


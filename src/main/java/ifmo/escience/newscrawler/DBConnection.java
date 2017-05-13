package ifmo.escience.newscrawler;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import ifmo.escience.newscrawler.helpers.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {
    static MongoClient mongoClient;
    static DB db;
    static DBCollection coll;
    static DBCollection missingCollection;
    static Logger logger = LogManager.getLogger(DBConnection.class.getName());

    public void insert(WebPage page) {
        try {
            BasicDBObject doc = new BasicDBObject();
            doc.append("Number", page.parseTime()).append("Title", page.getArticleName()).append("MainText", page.getArticleText());
            doc.append("Date", page.getArticleDate()).append("Link", page.getPageUrl()).append("MainLink", page.getEntityUrl());
            coll.insert(doc);
//            System.out.println("Document inserted successfully");
        } catch (Exception ex) {
            logger.error("Error on loading news into database!", ex);
        }
    }

    public boolean exists(String pageUrl) {
        return (coll.find(new BasicDBObject("Link", pageUrl)).count()) > 0 ? true : false;
    }

    public void addMissingLink(String link) {
        try {
            BasicDBObject document = (BasicDBObject) missingCollection.findOne(new BasicDBObject("url", link));
            if (document != null) {
                System.out.println(document);
                BasicDBObject newDocument =
                        new BasicDBObject().append("$inc",
                                new BasicDBObject().append("count", 1));
                missingCollection.update(new BasicDBObject().append("url", link), newDocument);
            } else {
                document = new BasicDBObject();
                document.append("url", link);
                document.append("count", 1);
                missingCollection.insert(document);
            }
        } catch (Exception ex) {
            logger.error("Error on adding missing link!", ex);
        }
    }

    public static void getDbConfigs() {
        String dbHost = ConfigReader.getProperty("dbHost");
        Integer dbPort = Integer.parseInt(ConfigReader.getProperty("dbPort"));
        String dbName = ConfigReader.getProperty("dbName");
        String dbCollection = ConfigReader.getProperty("dbCollection");
        String dbMissingCollection = ConfigReader.getProperty("dbMissingCollection");
        mongoClient = new MongoClient(dbHost, dbPort);
        db = mongoClient.getDB(dbName);
        coll = db.getCollection(dbCollection);
        missingCollection = db.getCollection(dbMissingCollection);
    }
}
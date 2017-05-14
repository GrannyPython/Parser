package ifmo.escience.newscrawler;

import ifmo.escience.newscrawler.entities.NewsPage;
import ifmo.escience.newscrawler.entities.YandexEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    private static Logger logger = LogManager.getLogger(Crawler.class.getName());
    private static Map<String, NewsPage> webEntitiesMap;
    private DBConnection dbConnection = new DBConnection();
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
        DBConnection.initConfigs();
        initEntities();
    }

    private static void initEntities() {
        ArrayList<YandexEntity> rootEntities = new ArrayList<>();
        for (NewsPage rootBase : getEntitiesList("config.json")) {
            rootEntities.add(new YandexEntity(rootBase));
        }
        logger.info("Starting root entities...");
        for (YandexEntity yandexEntity : rootEntities) {
            yandexEntity.start();
            logger.info("Thread for " + yandexEntity.getEntityName() + " was created");
        }

        List<NewsPage> webEntities = getEntitiesList("multiConfig.json");
        webEntitiesMap = new HashMap<>();
        for (NewsPage aNewsPageList : webEntities) {
            String entityUrl = getUrlStd(aNewsPageList.getEntityUrl());
            webEntitiesMap.put(entityUrl, aNewsPageList);
        }

        for (NewsPage newsPage : webEntities) {
            newsPage.start();
            logger.info("Thread for " + newsPage.getEntityName() + " was created");
        }
    }

    private static List<NewsPage> getEntitiesList(String cfgPath) {
        List<NewsPage> newsPageList = new ArrayList<>();
        try {
            newsPageList = mapper.readValue(new File(cfgPath),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, NewsPage.class));
        } catch (IOException ex) {
            logger.error("Error on getting links from config!", ex);
        }
        return newsPageList;
    }

    private static String getUrlStd(String url) {
        //TODO: wtf is it?
        String urlRegex = "((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?" +
                "[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)" +
                "((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[.\\!\\/\\\\w]*))?)";
        Pattern urlPattern = Pattern.compile(urlRegex);
        Matcher urlMatcher = urlPattern.matcher(url);
        if (urlMatcher.find()) {
            return urlMatcher.group(2);
        } else return "";
    }

    private boolean routeLink(String link) {
        String linkUrl = getUrlStd(link);
        if (webEntitiesMap.containsKey(linkUrl)) {
            NewsPage entityForLink = webEntitiesMap.get(linkUrl);
            entityForLink.transmitToParser(link);
            return true;
        } else {
            dbConnection.addMissingLink(linkUrl);
            return false;
        }
    }


    public void addLinks(List<String> links) {
        float goodLinks = 0;
        for (int i = 0; i < links.size(); i++) {
            if (routeLink(links.get(i)))
                goodLinks++;
        }
        if (!links.isEmpty())
            System.out.println(goodLinks / links.size() * 100);

    }

}

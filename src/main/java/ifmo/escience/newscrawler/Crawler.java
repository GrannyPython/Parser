package ifmo.escience.newscrawler;

import ifmo.escience.newscrawler.entities.RootEntity;
import ifmo.escience.newscrawler.entities.WebEntity;
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
    private Logger logger = LogManager.getLogger(Crawler.class.getName());
    private Map<String, WebEntity> webEntities;
    private DBConnection dbConnection = new DBConnection();
    ObjectMapper mapper = new ObjectMapper();

    private List<WebEntity> getEntitiesList(String cfgPath) {
        List<WebEntity> webEntityList = new ArrayList<>();
        try {
            webEntityList = mapper.readValue(new File(cfgPath),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, WebEntity.class));
        } catch (IOException ex) {
            logger.error("Error on getting links from config!", ex);
        }
        return webEntityList;
    }

    void start() throws IOException, InterruptedException {
        ArrayList<RootEntity> rootEntities = new ArrayList<RootEntity>();
        List<WebEntity> rootEntitiesBase = getEntitiesList("config.json");
        for (WebEntity rootBase : rootEntitiesBase) {
            rootEntities.add(new RootEntity(rootBase));
        }
        List<WebEntity> webEntities = getEntitiesList("multiConfig.json");
        if (webEntities == null) {
            logger.error("List of entities in empty!");
            return;
        }
        fillMap(webEntities);

        for (WebEntity webEntity : webEntities) {
            webEntity.start();
            logger.info("Thread for " + webEntity.getEntityName() + " was created");
        }
        logger.info("Starting root entities...");
        for (RootEntity rootEntity : rootEntities) {
            rootEntity.setCrawler(this);
            rootEntity.start();
            logger.info("Thread for " + rootEntity.getEntityName() + " was created");
        }
    }

    private void fillMap(List<WebEntity> webEntityList) {
        webEntities = new HashMap<>();
        for (WebEntity aWebEntityList : webEntityList) {
            String entityUrl = aWebEntityList.getEntityUrl();
            entityUrl = getUrlStd(entityUrl);
            aWebEntityList.setCrawler(this);
            webEntities.put(entityUrl, aWebEntityList);
        }
    }

    private String getUrlStd(String url) {
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
        if (webEntities.containsKey(linkUrl)) {
            WebEntity entityForLink = webEntities.get(linkUrl);
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
            boolean res = routeLink(links.get(i));
            if (res)
                goodLinks++;
        }
        if (!links.isEmpty())
            System.out.println(goodLinks / links.size() * 100);

    }

}

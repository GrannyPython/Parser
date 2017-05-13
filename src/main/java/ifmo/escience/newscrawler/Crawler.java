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
    Logger logger = LogManager.getLogger(Crawler.class.getName());
    private Map<String, WebEntity> webEntities;
    DBConnection dbConnection = new DBConnection();

    public ArrayList<WebEntity> getEntitiesList(String cfgPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<WebEntity> webEntityList = mapper.readValue(new File(cfgPath),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, WebEntity.class));
            return webEntityList;
        } catch (IOException ex) {
            logger.error("Error on getting links from config!", ex);
            return null;
        }
    }

    public void start() throws IOException, InterruptedException {
        ArrayList<RootEntity> rootEntities = new ArrayList<RootEntity>();
        ArrayList<WebEntity> rootEntitiesBase = getEntitiesList("config.json");
        for (WebEntity rootBase : rootEntitiesBase) {
            rootEntities.add(new RootEntity(rootBase));
        }
        ArrayList<WebEntity> webEntities = getEntitiesList("multiConfig.json");
        if (webEntities == null) {
            logger.error("List of entities in empty!");
            return;
        }
        fillMap(webEntities);

        for (int i = 0; i < webEntities.size(); i++) {
            webEntities.get(i).start();
            logger.info("Thread for " + webEntities.get(i).getEntityName() + " was created");
        }
        logger.info("Starting root entities...");
        for (int i = 0; i < rootEntities.size(); i++) {
            rootEntities.get(i).setCrawler(this);
            rootEntities.get(i).start();
            logger.info("Thread for " + rootEntities.get(i).getEntityName() + " was created");
        }
    }

    private void fillMap(ArrayList<WebEntity> webEntityList) {
        webEntities = new HashMap<String, WebEntity>();
        for (int i = 0; i < webEntityList.size(); i++) {
            String entityUrl = webEntityList.get(i).getEntityUrl();
            entityUrl = getUrlStd(entityUrl);
            webEntityList.get(i).setCrawler(this);
            webEntities.put(entityUrl, webEntityList.get(i));
        }
    }

    private String getUrlStd(String url) {
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

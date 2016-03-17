package com.company;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    Logger CrawlerLog =  LogManager.getLogger(Main.class.getName());;
    private Map<String, WebEntity> webEntityMap;
    private ArrayList<String> linksToWebPages = new ArrayList<String>();


    public void startDuty() throws IOException, InterruptedException {
        ArrayList<WebEntity> webEntityList = WebEntity.getEntityListFromConfig("src/main/resources/config/multiConfig.json");
        fillMap(webEntityList);
        Thread[] threads = new Thread[webEntityList.size()];

        while (true) {
            for (int i = 0; i < webEntityList.size(); i++) {
                threads[i] = new Thread(webEntityList.get(i));
                threads[i].start();
                CrawlerLog.info("Thread for " + webEntityList.get(i).getEntityName() + " was created");
            }

            Thread.sleep(600000);

            for (int i = 0; i < webEntityList.size(); i++) {
                threads[i].join();
                CrawlerLog.info("Thread for " + webEntityList.get(i).getEntityName() + " succesfully ended");
            }
        }
    }

    private void fillMap(ArrayList<WebEntity> webEntityList) {
        webEntityMap = new HashMap<String, WebEntity>();
        for (int i = 0; i < webEntityList.size(); i++) {
            String entityUrl = webEntityList.get(i).getEntityUrl();
            entityUrl = getUrlStd(entityUrl);

            webEntityMap.put(entityUrl, webEntityList.get(i));

            webEntityList.get(i).setCrawlerForCurrentEntity(this);
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

    private void processLinks() {
        for (int i = 0; i < linksToWebPages.size(); i++) {
            routeLink(linksToWebPages.get(i));
        }
        linksToWebPages.clear();
    }

    private void routeLink(String link) {
        String linkUrl = getUrlStd(link);
        if (webEntityMap.containsKey(linkUrl)) {
            WebEntity entityForLink = webEntityMap.get(linkUrl);
            entityForLink.transmitToParser(link);
        }

    }

    public void addLinks(ArrayList<String> links) {
        linksToWebPages.addAll(links);
        processLinks();
    }
}

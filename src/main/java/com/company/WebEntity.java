package com.company;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@JsonIgnoreProperties(ignoreUnknown = true)
public class WebEntity implements Runnable {

    private String entityName;
    private String entityUrl;
    private String newsListPath;
    private String articleNamePath;
    private String articleDatePath;
    private String articleTextPath;
    private String parserMode; //May be JSoup or Selenium. Field for future investigations.
    private long refreshTimeout;
    static Log mLog = LogFactory.getLog("MainClassLogger");
    private Crawler crawlerForCurrentEntity;

    private WebPageParser parserForCurrentEntity;
    //Single-configuration file
    public static WebEntity getEntityFromConfig(String cfgPath) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        WebEntity wEntity = mapper.readValue(new File(cfgPath), WebEntity.class);
        return wEntity;
    }

    //Able to load multi-configuration files.
    public static ArrayList<WebEntity> getEntityListFromConfig(String cfgPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ArrayList<WebEntity> webEntityList = mapper.readValue(new File(cfgPath),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, WebEntity.class));

        return webEntityList;
    }

    public String saveToConfig(IEntity entity) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File conf = new File("config/myObj.json");
        mapper.writeValue(conf, entity);

        return conf.getAbsolutePath();
    }

    @Override
    public String toString() {
        return (this.entityName
                + "\n" + this.entityUrl
                + "\n" + this.newsListPath);
    }

    /*child method*/
    private ArrayList<WebPage> getLinksFromTheMainSite(String MainLink) throws Exception {
        ArrayList<WebPage> arrayOfWebPages = new ArrayList<WebPage>();
        if (parserMode.equals("jsoup")) {
            Document doc = Jsoup.connect(entityUrl).get();
            Elements blockTitle = doc.select(newsListPath);
            Elements OnlyLinks = blockTitle.select("a[href]");
            String newsUrl = regExp(entityUrl);
            ArrayList<String> ArrayListNewsLinksInDB = new ArrayList<String>();
            for (int i = 0; i < OnlyLinks.size(); i++) {
                if (((OnlyLinks.get(i)).attr("href").toString()).indexOf("http") == -1) {
                    String pageUrl = newsUrl.substring(0, newsUrl.length() - 1) + OnlyLinks.get(i).attr("href").toString();
                    if (ArrayListNewsLinksInDB.size() == 0) {
                        WebPage newPage = new WebPage(this, pageUrl);
                        arrayOfWebPages.add(newPage);
                    } else {
                        for (int j = 0; j < ArrayListNewsLinksInDB.size(); j++) {
                            if ((ArrayListNewsLinksInDB.get(j)).equals(pageUrl)) {
                                break;
                            } else {
                                if ((j == ArrayListNewsLinksInDB.size() - 1)) {
                                    WebPage newPage = new WebPage(this, pageUrl);
                                    arrayOfWebPages.add(newPage);
                                }
                            }
                        }
                    }
                }
            }
        }
    else {
            WebDriver driver = new HtmlUnitDriver();
            driver.get(entityUrl);

        List<WebElement> OnlyLinks = driver.findElements(By.xpath(newsListPath));

        for (WebElement link : OnlyLinks){
            WebPage newPage = new WebPage(this, link.getAttribute("href"));
            arrayOfWebPages.add(newPage);
        }
    }
    return arrayOfWebPages;
    }



    public void run() {
        WebPageParser pageParser = null;
        try {
            pageParser = new WebPageParser(getLinksFromTheMainSite(entityUrl));
            parserForCurrentEntity = pageParser;
            pageParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void main(String[] args) throws Exception {
        this.run();
    }

    private String regExp(String entityUrl) throws Exception {
        String LinkAfterRegExp = null;
        Pattern urlPattern = Pattern.compile("^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})\\/");
        Matcher urlMatcher = urlPattern.matcher(entityUrl);
        while (urlMatcher.find()) {
            LinkAfterRegExp = (entityUrl.substring(urlMatcher.start(), urlMatcher.end()) + "");
        }
        return LinkAfterRegExp;
    }

    public void transmitToCrawler(ArrayList<String> links) {
        crawlerForCurrentEntity.addLinks(links);
    }

    public void transmitToParser(String link) {
        WebPage page = new WebPage(this, link);
        parserForCurrentEntity.addPage(page);
    }


    public void setCrawlerForCurrentEntity(Crawler crawlerForCurrentEntity) {
        this.crawlerForCurrentEntity = crawlerForCurrentEntity;
    }

    public String getArticleTextPath() {
        return articleTextPath;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityUrl() {
        return entityUrl;
    }

    public String getNewsListPath() {
        return newsListPath;
    }

    public String getArticleNamePath() {
        return articleNamePath;
    }

    public String getArticleDatePath() {
        return articleDatePath;
    }

    public String getParserMode() {
        return parserMode;
    }

    public long getRefreshTimeout() {
        return refreshTimeout;
    }
}
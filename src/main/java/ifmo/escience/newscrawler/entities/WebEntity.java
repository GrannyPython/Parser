package ifmo.escience.newscrawler.entities;

import ifmo.escience.newscrawler.Crawler;
import ifmo.escience.newscrawler.WebPageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class WebEntity extends Thread {

    protected static Logger logger = LogManager.getLogger(WebEntity.class.getName());
    protected String entityName,
            entityUrl,
            newsListPath,
            articleNamePath,
            articleDatePath,
            articleTextPath,
            tags = "",
            similarNews = "",
            regExpForDate,
            dateFormat;
    protected long refreshTimeout;

    protected Crawler crawler;
    protected WebPageParser parser = new WebPageParser(new ArrayList<>(), this);


    static {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.SEVERE);
    }

    public void setCrawler(Crawler crawler) {
        this.crawler = crawler;
    }

    public void transmitToParser(String link) {
        parser.addPage(link);
    }

    public void transmitToCrawler(List<String> links) {
        crawler.addLinks(links);
    }

    @Override
    public String toString() {
        return (this.entityName
                + "\n" + this.entityUrl
                + "\n" + this.newsListPath);
    }

    protected List<String> getLinks(String targetUrl) throws Exception {
        List<String> arrayOfWebPages = new ArrayList<>();
        HtmlUnitDriver driver = new HtmlUnitDriver();
        logger.error("Loading links from: " + targetUrl);
        driver.get(targetUrl);
        if (newsListPath != "") {
            List<WebElement> links = driver.findElements(By.xpath(newsListPath));

            for (WebElement link : links) {
                String href = link.getAttribute("href");
                arrayOfWebPages.add(href);
            }
        }

        return arrayOfWebPages;
    }

    @Override
    public void run() {
        try {
            List<String> links = getLinks(this.entityUrl);
            for (String link : links) {
                parser.addPage(link);
            }
            while (true) {
                parser.parse();
                Thread.sleep(refreshTimeout);
            }
        } catch (Exception e) {
            logger.error("Error on collecting web pages!", e);
        }
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

    public String getTagsPath() {
        return tags;
    }

    public String getSimilarNewsPath() {
        return similarNews;
    }

    public long getRefreshTimeout() {
        return refreshTimeout;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getRegExpForDate() {
        return regExpForDate;
    }

    public void setRegExpForDate(String regExpForDate) {
        this.regExpForDate = regExpForDate;
    }

}

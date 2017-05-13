package ifmo.escience.newscrawler.entities;

import ifmo.escience.newscrawler.ProxyManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RootEntity extends WebEntity {
    Random rand = new Random();
    HtmlUnitDriver driver = new HtmlUnitDriver();

    public RootEntity(WebEntity from) {
        this.newsListPath = from.newsListPath;
        this.entityName = from.entityName;
        this.entityUrl = from.entityUrl;

    }

    @Override
    public void run() {
        int syncDays = 60;
        int time = 45_000 + rand.nextInt(30_000) + 1;

        StringBuffer addon = new StringBuffer();
        try {
            while (true) {
                LocalDateTime currentDate = LocalDateTime.now();
                for (int day = 0; day < syncDays; day++) {
                    LocalDateTime newDate = currentDate.minusDays(1);
                    for (int page = 0; page < 83; page++) {
                        addon.delete(0, addon.length());
                        addon.append(this.entityUrl);
                        addon.append("&from_day=" + String.valueOf(newDate.getDayOfMonth()));
                        addon.append("&from_month=" + String.valueOf(newDate.getMonthValue()));
                        addon.append("&from_year=" + String.valueOf(newDate.getYear()));
                        addon.append("&to_day=" + String.valueOf(currentDate.getDayOfMonth()));
                        addon.append("&to_month=" + String.valueOf(currentDate.getMonthValue()));
                        addon.append("&to_year=" + String.valueOf(currentDate.getYear()));
                        addon.append("&p=" + String.valueOf(page));
                        List<String> links = null;
                        do {
                            do {
                                try {
                                    Thread.sleep(2_000);
                                    links = getLinks(addon.toString());
                                } catch (SocketTimeoutException exception) {
                                    System.out.println("BadProxy1");
                                } catch (ConnectTimeoutException exception) {
                                    System.out.println("Bad proxy2");
                                } catch (Exception e) {
                                    System.out.println("BadProxy3");
                                }
                            } while (links == null);
                        } while (links.isEmpty());
                        Thread.sleep(time);
                    }
                    currentDate = newDate;
                }
            }
        } catch (Exception ex) {
            logger.error("Error on collecting web pages!", ex);
        }

    }

    @Override
    protected List<String> getLinks(String targetUrl) throws Exception {
        logger.trace("Loading links from: " + targetUrl);

        List<String> arrayOfWebPages = new ArrayList<>();
        Proxy proxy = new Proxy();
        String proxyFromSet = ProxyManager.getProxy();
        proxy.setSslProxy(proxyFromSet);
        driver.setProxySettings(proxy);
        driver.get(targetUrl);

        List<WebElement> links = driver.findElements(By.xpath(newsListPath));
        if (links.size() > 0) {
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                arrayOfWebPages.add(href);
            }
            ProxyManager.feedBack(proxyFromSet);
            crawler.addLinks(arrayOfWebPages);
        }
        return arrayOfWebPages;
    }
}

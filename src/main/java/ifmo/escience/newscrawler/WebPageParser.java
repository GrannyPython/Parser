package ifmo.escience.newscrawler;

import ifmo.escience.newscrawler.entities.NewsPage;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebPageParser {
    HtmlUnitDriver driver = new HtmlUnitDriver();

    private static Logger logger = LogManager.getLogger(WebPageParser.class.getName());

    DBConnection dbConnection = new DBConnection();

    private List<String> webLinks;
    private NewsPage entity;

    static {
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
    }

    public WebPageParser(NewsPage entity) {
        this.webLinks = new ArrayList<>();
        this.entity = entity;
    }

    public void addPage(String page) {
        webLinks.add(page);
    }

    public void parse() {
        while (webLinks.size() != 0) {
            try {
                parsePage(webLinks.get(0));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Web page is not starting " + webLinks.get(0));
            }
            webLinks.remove(0);
        }
    }

    private void parsePage(String link) throws Exception {
        if (!dbConnection.exists(link)) {
            WebPage newPage = new WebPage(link);
            newPage.setParseTime(System.nanoTime());

            driver.get(newPage.getPageUrl());

            String Header = driver.findElement(By.xpath(entity.getArticleNamePath())).getText();
            List<WebElement> BodyElement = driver.findElements(By.xpath(entity.getArticleTextPath()));
            String Body = "";
            for (WebElement element : BodyElement) {
                Body += element.getText();
            }

            String Tags = null;
            if (!entity.getTagsPath().isEmpty()) {
                List<WebElement> TagsElement = driver.findElements(By.xpath(entity.getTagsPath()));
                List<String> tagsList = new LinkedList<String>();
                for (WebElement element : TagsElement) {
                    tagsList.add(element.getText());
                }
                Tags = tagsList.toString();
            }
            newPage.setTags(Tags);

            List<String> similarLinks = new LinkedList<String>();
            if (!entity.getSimilarNewsPath().isEmpty()) {
                List<WebElement> OnlyLinks = driver.findElements(By.xpath(entity.getSimilarNewsPath()));
                for (WebElement similarLink : OnlyLinks) {
                    similarLinks.add(similarLink.getAttribute("href"));
                }
            }

            String date = driver.findElement(By.xpath(entity.getArticleDatePath())).getText();
            String articleDate = (checkWords(doRegExp(date.replaceAll("\n", " ")), entity.getDateFormat()));
            System.out.println(articleDate + " " + newPage.getPageUrl() + "\n" + Header + "\n" + Body
                    + "\n" + Tags + similarLinks);

            newPage.setArticleName(Header);
            newPage.setArticleText(Body);
            newPage.setArticleDate(articleDate);
            newPage.setSimilarNews(similarLinks.toString());
            dbConnection.insert(newPage);
        }
    }

    private String checkWords(String date, String datePattern) throws ParseException {
        String hour, minute, day, month, year;
        LocalDateTime now = LocalDateTime.now();
        date = date.toLowerCase().replaceAll(",", " ").replaceAll(" +", " ").replaceAll("-", " ").trim();
        Calendar calendar = Calendar.getInstance(); // this would default to now

        if ((date.contains("cегодня")) | (date.contains("сегодня"))) {
            year = String.valueOf(now.getYear());
            day = String.valueOf(String.format("%02d", now.getDayOfMonth()));
            month = String.valueOf(String.format("%02d", now.getMonthValue()));
            String a = day + "." + month + "." + year;
            date = date.replaceAll("сегодня", a).replaceAll("сегодня", a).replaceAll("[)(]", "");
        }

        if (date.toLowerCase().contains("вчера")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            calendar.add(Calendar.DATE, -1);
            //System.out.println("date = [" + date + "], datePattern = [" + datePattern + "]");
            year = String.valueOf(now.getYear());
            day = String.valueOf(String.format("%02d", calendar.getTime().getDate()));
            month = String.valueOf(String.format("%02d", calendar.getTime().getMonth() + 1));
            String a = day + "." + month + "." + year;
            date = date.replaceAll("вчера", a);
        }

        Locale rusLocale = new Locale.Builder().setLanguage("ru").build();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, rusLocale);

            Date dateFinal = dateFormat.parse(date);

            if (datePattern.contains("yyyy")) {
                year = String.valueOf(dateFinal.getYear() + 1900);
            } else {
                year = String.valueOf(now.getYear());
            }

            day = String.valueOf(String.format("%02d", dateFinal.getDate()));
            month = String.valueOf(String.format("%02d", dateFinal.getMonth() + 1));
            hour = String.valueOf(String.format("%02d", dateFinal.getHours()));
            if (Integer.parseInt(hour) < 0)
                hour = String.valueOf((24 + Integer.parseInt(hour)));
            minute = String.valueOf(String.format("%02d", dateFinal.getMinutes()));
            date = day + "." + month + "." + year + " " + hour + ":" + minute;
        } catch (ParseException e) {
        }
        return date;
    }

    private void dateProc(String date, int monthNumber) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        String result = month + "." + year;
    }

    private String doRegExp(String date) {

        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile(entity.getRegExpForDate()).matcher(date);
        while (m.find()) {
            allMatches.add(m.group());
        }

        return allMatches.size() > 0 ? allMatches.get(0) : date;
    }


    private String articleTextProcessing(String text, WebPage page) {

        String entityUrl = page.getEntityUrl();

        String eraseRegex = "<[a-zA-Z\\/][^>]*>|&nbsp;|\\n";
        String hrefRegex = "(<a.+?\\s*href\\s*=)\\s*[\"\\']?([^\"\\'\\s>]+)[\"\\']?";

        Pattern hrefPattern = Pattern.compile(hrefRegex);
        Matcher hrefMatcher = hrefPattern.matcher(text);

        ArrayList<String> linksFromText = new ArrayList<String>();

        while (hrefMatcher.find()) {
            String foundHref = hrefMatcher.group(2);
            if (!isUrl(foundHref)) {
                foundHref = makeUrl(foundHref, entityUrl);
            } else foundHref = correctUrl(foundHref, entityUrl);
            linksFromText.add(foundHref);
        }

        if (linksFromText.size() > 0) {
            transmitLinksToCrawler(linksFromText, page);
        }


        //TODO regExp add for &#x97
        String resultText = text.replaceAll(eraseRegex, " ").replaceAll("(?s)<!--.*?-->", " ").replaceAll("&#x200b", " ").replaceAll("&#x97", " ").replaceAll("&nbsp;", " ");
        return resultText;

    }

    private boolean isUrl(String checkedString) {
        String urlRegex = "((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?" +
                "[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)" +
                "((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[.\\!\\/\\\\w]*))?)";
        Pattern urlPattern = Pattern.compile(urlRegex);
        Matcher urlMatcher = urlPattern.matcher(checkedString);
        if (urlMatcher.find()) {
            return true;
        } else return false;
    }

    private String makeUrl(String interLink, String entityUrl) {
        String urlRegex = "((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?" +
                "[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)" +
                "((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[.\\!\\/\\\\w]*))?)";
        Pattern urlPattern = Pattern.compile(urlRegex);
        Matcher urlMatcher = urlPattern.matcher(entityUrl);
        String targetUrl = entityUrl;
        if (urlMatcher.find()) {
            targetUrl = urlMatcher.group(2);
        }
        return targetUrl.concat(interLink);
    }

    private String correctUrl(String url, String entityUrl) { //Simple correction for fontanka. Not good, I know.
        String correctedUrl = url;
        System.out.println(url);
        String checkRegex = "(https?:\\/\\/)(www.)(\\w*)\\.(\\w*)((\\W\\w*)*)";
        Pattern checkPattern = Pattern.compile(checkRegex);
        Matcher matcher = checkPattern.matcher(url);
        if (matcher.find() && entityUrl == "http://m.fontanka.ru/") {
            correctedUrl = makeUrl(matcher.group(5), entityUrl);
        }

        return correctedUrl;
    }

    private void transmitLinksToCrawler(ArrayList<String> links, WebPage page) {
        entity.transmitToCrawler(links);
    }
}

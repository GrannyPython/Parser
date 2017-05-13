package ifmo.escience.newscrawler;

public class WebPage {
    private Long parseTime;
    private String entityUrl;
    private String pageUrl;
    private String articleName;
    private String articleDate;
    private String articleText;
    private String tags;
    private String similarNews;

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSimilarNews() {
        return similarNews;
    }

    public void setSimilarNews(String sameNews) {
        this.similarNews = sameNews;
    }

    public Long parseTime() {
        return parseTime;
    }

    public void setParseTime(Long parseTime) {
        this.parseTime = parseTime;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setArticleDate(String articleDate) {
        this.articleDate = articleDate;
    }

    public void setArticleText(String articleText) {
        this.articleText = articleText;
    }

    public String getEntityUrl() {
        return entityUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getArticleName() {
        return articleName;
    }

    public String getArticleDate() {
        return articleDate;
    }

    public String getArticleText() {
        return articleText;
    }

    public WebPage(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}

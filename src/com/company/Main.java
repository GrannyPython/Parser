package com.company;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;

public class Main {
    //http://www.gazeta.spb.ru/2/0/
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("http://www.gazeta.spb.ru/2/0/").get();

        Elements blockTitle = doc.select("div.blockTitle.size14.nonLine");
        Elements OnlyLinks = blockTitle.select("a[href]");

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < OnlyLinks.size() - 1; i++) {
            System.out.println((OnlyLinks.get(i)).childNode(0).toString());//title
            System.out.println((OnlyLinks.get(i)).attr("href").toString());//href
        }
    }
}e

package com.company;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.sql.*;
import java.util.ArrayList;
import java.sql.SQLException;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/MyBase1";
    private static final String user = "root";
    private static final String password = "123";
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;


    //http://www.gazeta.spb.ru/2/0/

    public static void main(String[] args) throws Exception {
        String MyLink = "http://www.gazeta.spb.ru/2/0/";
        ArrayList<String> Links = new ArrayList<String>();
        Links = PLinks(MyLink);
        ArrayList<String> LinkAddit = new ArrayList<String>();
        ArrayList<String> InfoIntoDB = new ArrayList<String>();

        // for(int i = 0; i < Links.size()-1; i++) {
        int i = 0;
        LinkAddit.add(MyLink.substring(0, MyLink.length() - 5) + Links.get(i));//delete last slash, remove regexp later
        InfoIntoDB = ParsePage(LinkAddit.get(i));
        InfoIntoDB.add(0, Integer.toString(i));
        InfoIntoDB.add(LinkAddit.get(i));
        InfoIntoDB.add(MyLink);
        PutIntoDB(InfoIntoDB);
    }

    public static ArrayList<String> ParsePage(String ML) throws Exception {
        Document doc1 = Jsoup.connect(ML).get();
        ArrayList<String> InfoAboutLink = new ArrayList<String>();

        Elements Header = doc1.select("h1");//title
        String Header_Text = Header.get(0).textNodes().toString();
        InfoAboutLink.add(Header_Text);
        //main header on main page
        Elements blockTitle1 = doc1.select("div#ntext");
        String s = (blockTitle1.get(0).textNodes()).toString(); //main text
        //add text correct between transger into string
        InfoAboutLink.add(s);
        //time on main page
        Elements Time = doc1.select("i");
        String Time_Text = Time.get(1).textNodes().toString();
        InfoAboutLink.add(Time_Text);
        return InfoAboutLink;
    }

    public static ArrayList<String> PLinks(String ML) throws Exception {
        Document doc = Jsoup.connect(ML).get();
        Elements blockTitle = doc.select("div.blockTitle.size14.nonLine");
        Elements OnlyLinks = blockTitle.select("a[href]");
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < OnlyLinks.size() - 1; i++) {
            list.add(((OnlyLinks.get(i)).attr("href").toString()));//href
        }
        return list;
    }

    /*, MyTitle, MyMainText, MyDate, MyLink, MyMainLink*/
    /*, InfoIntoDB.get[1], InfoIntoDB.get(2), InfoIntoDB.get(3), InfoIntoDB.get(4), InfoIntoDB.get(5)*/
    public static void PutIntoDB(ArrayList<String> InfoIntoDB) throws SQLException {
      /*  String query = ("INSERT INTO MyBase1.MyTable1 (MyNumber)" +
                " VALUES (String InfoIntoDB.get(0));");*/
        /**/
        String query1 = "INSERT INTO MyTable1 (MyNumber, MyTitle, MyMainText, MyDate, MyLink, MyMainLink)" +
                " VALUES ('"  + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(0) + "');";

        System.out.println(query1);
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
            stmt.executeUpdate(query1);

    }
}



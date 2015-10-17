package com.company;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.sql.*;
import java.util.ArrayList;
import java.sql.SQLException;


public class Main {

    private static final String url = "jdbc:mysql://localhost:3306/MyBase1";
    private static final String user = "root";
    private static final String password = "123";
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    //http://www.gazeta.spb.ru/allnews/
    //http://www.fontanka.ru/


    //String SelectorLinksFromMainSite =//"div.materials.nonLine";              // for gazeta
                                        //"div.entry.article.switcher-of-news"  // for Fontank


    public static void main(String[] args) throws Exception {
       // ArrayList<String> PackOfSettingsGazeta = new ArrayList<String>("http://www.gazeta.spb.ru/allnews/", 9,
        // "div.materials.nonLine", "h1",  "div#ntext", "i" );
       // ArrayList<String> PacjOfSettingsFontan = new ArrayList<String>("http://m.fontanka.ru/", 1,
        // "li.article.switcher-all-news", "h2.itemTitle", "div.itemFullText", "span.itemDateCreated");


        String MainSiteLink = "http://m.fontanka.ru/";
        ArrayList<String> ArrayLinksFromTheMainSite = new ArrayList<String>();
        ArrayLinksFromTheMainSite = GetLinksFromTheMainSite(MainSiteLink);
        ArrayList<String> ArrayInfoIntoDB = new ArrayList<String>();
        ArrayList<String> LinksFromMainSite = new ArrayList<String>();
        String a = TakeLastNewsLinkFromDB();

        for (int i = 0; i < ArrayLinksFromTheMainSite.size(); i++) {
            System.out.println(a);
            System.out.println(MainSiteLink.substring(0, MainSiteLink.length() - 1) + ArrayLinksFromTheMainSite.get(i));//HERE

            if ((MainSiteLink.substring(0, MainSiteLink.length() - 1) + ArrayLinksFromTheMainSite.get(i)).equals(a))
            {
                break;
            }

            else

            {
                LinksFromMainSite.add(MainSiteLink.substring(0, MainSiteLink.length() - /**/1) + ArrayLinksFromTheMainSite.get(i));//Here 9 1
                ArrayInfoIntoDB = ParsePage(LinksFromMainSite.get(i));
                ArrayInfoIntoDB.add(0, Integer.toString(i));
                ArrayInfoIntoDB.add(LinksFromMainSite.get(i));
                ArrayInfoIntoDB.add(MainSiteLink);
                PutIntoDB(ArrayInfoIntoDB);
            }
        }

        System.out.println("Total number of news in the table : " + ShowNumOfNewsInDB());
    }

    public static ArrayList<String> ParsePage(String MainLink) throws Exception {
        Document doc1 = Jsoup.connect(MainLink).get();
        ArrayList<String> InfoAboutLink = new ArrayList<String>();

        Elements Header = doc1.select("h2.itemTitle");                //title for gaz = h1
        String Header_Text = Header.get(0).textNodes().toString();
        InfoAboutLink.add(Header_Text);
        //main header on main page
        Elements blockTitle1 = doc1.select("div.itemFullText"); // MainText gaz = div#ntext  Font = div.itemFullText
        String s = (blockTitle1.get(0).text()).toString(); //main text
        //add text correct between transger into string
        InfoAboutLink.add(s);
        //time on main page
        Elements Time = doc1.select("span.itemDateCreated");                       //Time span.itemDateCreated for F i for gaz
        String Time_Text = Time.get(0).textNodes().toString().replace("&nbsp;"," ");
        InfoAboutLink.add(Time_Text);
        return InfoAboutLink;
    }

    public static ArrayList<String> GetLinksFromTheMainSite(String MainLink) throws Exception {
        Document doc = Jsoup.connect(MainLink).get();
        Elements blockTitle = doc.select("li.article.switcher-all-news");///<<<!!!!!!!!!!
        Elements OnlyLinks = blockTitle.select("a[href]");         ///<<<!!!!!!!!!!
        ArrayList<String> ArrayOfLinks = new ArrayList<String>();
        for (int i = 0; i < OnlyLinks.size() - 1; i++) {
            if (((OnlyLinks.get(i)).attr("href").toString()).indexOf("http")==-1)
            {
                ArrayOfLinks.add(((OnlyLinks.get(i)).attr("href").toString()).replace("\'", ""));//href
            }
        }
        return ArrayOfLinks;
    }

    public static void PutIntoDB(ArrayList<String> InfoIntoDB) throws SQLException {
        PreparedStatement stmt = null;
            con = DriverManager.getConnection(url, user, password);;
            stmt = con.prepareStatement("INSERT INTO MyTable1 (MyNumber, MyTitle, MyMainText, MyDate, MyLink, MyMainLink) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, InfoIntoDB.get(0));
            stmt.setString(2, InfoIntoDB.get(1));
            stmt.setString(3, InfoIntoDB.get(2));
            stmt.setString(4, InfoIntoDB.get(3));
            stmt.setString(5, InfoIntoDB.get(4));
            stmt.setString(6, InfoIntoDB.get(5));
            stmt.executeUpdate();
    }

    public static ArrayList<String> TakeFromDB() throws SQLException {

        ArrayList<String> ArrayListInformFromDB = new ArrayList<String>();
        String query1 = "select MyNumber, MyTitle, MyMainText, MyDate, MyLink, MyMainLink from MyTable1";
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        rs = stmt.executeQuery(query1);

        while (rs.next()) {
            ArrayListInformFromDB.add(rs.getString(1));
            ArrayListInformFromDB.add(rs.getString(2));
            ArrayListInformFromDB.add(rs.getString(3));
            ArrayListInformFromDB.add(rs.getString(4));
            ArrayListInformFromDB.add(rs.getString(5));
            ArrayListInformFromDB.add(rs.getString(6));
        }
        System.out.println(query1);
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
    return ArrayListInformFromDB;
    }

    public static  int ShowNumOfNewsInDB() {
        int count = 0;
        String query = "select count(*) from MyTable1";

        try {
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                count = rs.getInt(1);
            }
        }
        catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { con.close(); } catch(SQLException se) { /*can't do anything */ }
            try { stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try { rs.close(); } catch(SQLException se) { /*can't do anything */ }
        }
        return count;
    }

    public static String TakeLastNewsLinkFromDB() throws SQLException {
        String LinkOfLastNewsInDB = null;
        String query1 = "select * FROM MyTable1 WHERE MyNumber = 0";

        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        rs = stmt.executeQuery(query1);

        while (rs.next()) {
           LinkOfLastNewsInDB =  ((rs.getString(5)));
        }
        return LinkOfLastNewsInDB;
    }
}


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
    //http://www.gazeta.spb.ru/2/0/


    public static void main(String[] args) throws Exception {
        String MainSiteLink = "http://www.gazeta.spb.ru/2/0/";
        ArrayList<String> ArrayLinksFromTheMainSite = new ArrayList<String>();
        ArrayLinksFromTheMainSite = GetLinksFromTheMainSite(MainSiteLink);
        ArrayList<String> ArrayInfoIntoDB = new ArrayList<String>();
        ArrayList<String> LinksFromMainSite = new ArrayList<String>();
        String a = TakeLastNewsFromDB();

        for (int i = 0; i < ArrayLinksFromTheMainSite.size(); i++) {
            System.out.println(a);
            System.out.println(MainSiteLink.substring(0, MainSiteLink.length() - 5) + ArrayLinksFromTheMainSite.get(i));

            if ((MainSiteLink.substring(0, MainSiteLink.length() - 5) + ArrayLinksFromTheMainSite.get(i)).equals(a))
            {
                break;
            }

            else

            {
                LinksFromMainSite.add(MainSiteLink.substring(0, MainSiteLink.length() - 5) + ArrayLinksFromTheMainSite.get(i));
                ArrayInfoIntoDB = ParsePage(LinksFromMainSite.get(i));
                ArrayInfoIntoDB.add(0, Integer.toString(i));
                ArrayInfoIntoDB.add(LinksFromMainSite.get(i));
                ArrayInfoIntoDB.add(MainSiteLink);
                PutIntoDB(ArrayInfoIntoDB);
            }
        }

        System.out.println("Total number of news in the table : " + ShowNumOfNewsInDB());
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

    public static ArrayList<String> GetLinksFromTheMainSite(String ML) throws Exception {
        Document doc = Jsoup.connect(ML).get();
        Elements blockTitle = doc.select("div.blockTitle.size14.nonLine");
        Elements OnlyLinks = blockTitle.select("a[href]");
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < OnlyLinks.size() - 1; i++) {
            list.add(((OnlyLinks.get(i)).attr("href").toString()));//href
        }
        return list;
    }

    public static void PutIntoDB(ArrayList<String> InfoIntoDB) throws SQLException {
        String query1 = "INSERT INTO MyTable1 (MyNumber, MyTitle, MyMainText, MyDate, MyLink, MyMainLink)" +
                " VALUES ('" + InfoIntoDB.get(0) + "', '" + InfoIntoDB.get(1) + "', '" + InfoIntoDB.get(2) + "', '" + InfoIntoDB.get(3) + "', '" + InfoIntoDB.get(4) + "', '" + InfoIntoDB.get(5) + "');";

       // System.out.println(query1);
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        stmt.executeUpdate(query1);
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

    public static String TakeLastNewsFromDB() throws SQLException {
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


package ifmo.escience.newscrawler;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdditionalFunctionality {

    private static String path = "/home/gp/Desktop/DicWithTeacher/resources/news.json";

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        String[] a = readText(path);
    }

    static String[] readText(String path) throws IOException, ParseException, InterruptedException {
        JSONParser parser = new JSONParser();
        JSONArray obj = ((JSONArray) parser.parse(new FileReader(path)));
        List col = new ArrayList();

        for (int i = 0; i < 100; i++) {
            String a = String.valueOf(((JSONObject) obj.get(i)).get("MainText"));
            col.add(KeyWords.doIt(a));
            System.out.println(i);
            System.out.println(col.get(i));
        }

        writeToCSV(col);
        return null;
    }

    public static void writeToCSV(Collection list) {
        try {
            FileWriter writer = new FileWriter("/home/gp/Desktop/Text.csv");
            for (Object o : list) {
                writer.append(o.toString().replaceAll(",", " ").replaceAll("\n", " "));
                writer.append('\n');
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package ifmo.escience.newscrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class KeyWords {

    public static final String C_CODE_PATH = "/src/main/С/Debug/ConsoleApplication1.exe";

    public static String doIt(String news) throws IOException, InterruptedException {

        Set<String> set = new HashSet<String>();
        try { ProcessBuilder pb = new ProcessBuilder("mono", System.getProperty("user.dir") + C_CODE_PATH, news); Process p = pb.start(); BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream())); String line;
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty())
                    set.add(line.replaceAll("[\\pP\\s]", " "));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        StringBuilder sb = new StringBuilder();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            sb.append("\"").append(iterator.next()).append("\" ");
        }
        return sb.toString();
    }
}
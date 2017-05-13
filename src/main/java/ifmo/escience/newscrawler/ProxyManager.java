package ifmo.escience.newscrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gp on 30.03.16.
 */
public class ProxyManager {
    private static ProxyManager instance;
    private static Set<String> proxySet = new HashSet<>();
    private static Set<String> workableSet = new HashSet<>();
    private static Iterator<String> itr;

    ProxyManager() {
        itr = proxySet.iterator();
    }

    private static Set updateSet() throws IOException {
        URL url = new URL("http://txt.proxyspy.net/proxy.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.contains("+")) {
                Matcher m = Pattern.compile("\\d+.\\d+.\\d+.\\d+:\\d+ ").matcher(line);
                while (m.find()) {
                    proxySet.add(m.group().trim());
                }
            }
        }
        in.close();
        System.out.println(proxySet.size());
        return proxySet;
    }

    public static synchronized String getProxy() throws IOException {
        if (instance == null) {
            instance = new ProxyManager();
        }

        if (!itr.hasNext()) {
            if (workableSet.size() == 0) {
                proxySet = updateSet();

            } else {
                proxySet = workableSet;
                workableSet.clear();
            }
            itr = proxySet.iterator();
        }

        String id = itr.next();
        System.out.println(id);
        System.out.println(proxySet.size());
        return id;
    }

    public static synchronized void feedBack(String workableId) {
        workableSet.add(workableId);
    }
}




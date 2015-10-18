package com.company;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class WebEntity implements IEntity {

    public String entityName;
    public String entityUrl;
    public String newsListPath;
    public String articleNamePath;
    public String articleDatePath;
    public String articleTextPath;


    public WebEntity entityFromCfg(String cfgPath) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        WebEntity wEntity = mapper.readValue(new File(cfgPath), WebEntity.class);

        return wEntity;

    }

    public String entityToCfg(IEntity entity) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File conf = new File("config/myObj.json");
        mapper.writeValue(conf, entity);

        return conf.getAbsolutePath();
    }

    @Override
    public String toString() {
        return (this.entityName
                + "\n" + this.entityUrl
                + "\n" + this.newsListPath);
    }

    public static void main(String[] args) throws IOException {
        WebEntity we = new WebEntity();
        we = we.entityFromCfg("config/config.json");
        System.out.println(we.toString());
    }
}

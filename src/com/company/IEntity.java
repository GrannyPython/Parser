package com.company;

import java.io.IOException;

/**
 * Created by Easton on 19.10.2015.
 */
public interface IEntity {
    public IEntity entityFromCfg(String cfgPath) throws IOException;

    public String entityToCfg(IEntity entity) throws IOException;
}

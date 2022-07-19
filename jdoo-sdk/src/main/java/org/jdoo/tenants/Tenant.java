package org.jdoo.tenants;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jdoo.core.Loader;
import org.jdoo.core.Registry;
import org.jdoo.data.Database;

/**
 * 租户信息
 * 
 * @author lrz
 */
public class Tenant {
    private String key;
    private String name;
    private Map<String, Object> data = new HashMap<>();
    private Registry registry;
    private Database database;
    private Properties properties;

    public Tenant(String key, String name, Properties properties) {
        this.key = key;
        this.name = name;
        this.properties = properties;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Registry getRegistry() {
        if (registry == null) {
            synchronized (this) {
                if (registry == null) {
                    Database db = getDatabase();
                    registry = new Registry(this);
                    Loader.getLoader().loadModules(db, registry);
                }
            }
        }
        return registry;
    }

    public Properties getProperties() {
        return properties;
    }

    public Database getDatabase() {
        if (database == null) {
            database = new Database(properties);
        }
        return database;
    }

    public Map<String, Object> getData() {
        return data;
    }
}

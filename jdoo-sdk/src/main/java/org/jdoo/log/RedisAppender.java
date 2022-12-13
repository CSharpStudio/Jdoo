package org.jdoo.log;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    JedisPool pool;

    // keep this for config compatibility for now
    PlatformLogLayout jsonlayout;
    AuditLogLayout auditLayout;

    Layout<ILoggingEvent> layout;

    // logger configurable options
    String host = "localhost";
    int port = Protocol.DEFAULT_PORT;
    String key = null;
    int timeout = Protocol.DEFAULT_TIMEOUT;
    String password = null;
    int database = Protocol.DEFAULT_DATABASE;
    int timeBetweenEvictionRuns = 30000; //default in JedisPoolConfig
    int minEvictableIdleTime = 60000; //default in JedisPoolConfig

    public RedisAppender() {
        jsonlayout = new PlatformLogLayout();
        auditLayout = new AuditLogLayout();
    }

    @Override
    protected void append(ILoggingEvent event) {
        Jedis client = pool.getResource();
        try {
            String json = layout == null ? jsonlayout.doLayout(event) : layout.doLayout(event);
            client.rpush(key, json);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Deprecated
    public String getSource() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getSource();
        }
        return jsonlayout.getSource();
    }

    @Deprecated
    public void setSource(String source) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setSource(source);
        } else {
            jsonlayout.setSource(source);
        }
    }

    @Deprecated
    public String getSourceHost() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getSourceHost();
        }
        return jsonlayout.getSourceHost();
    }

    @Deprecated
    public void setSourceHost(String sourceHost) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setSourceHost(sourceHost);
        } else {
            jsonlayout.setSourceHost(sourceHost);
        }
    }

    @Deprecated
    public String getSourcePath() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getSourcePath();
        }
        return jsonlayout.getSourcePath();
    }

    @Deprecated
    public void setSourcePath(String sourcePath) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setSourcePath(sourcePath);
        } else {
            jsonlayout.setSourcePath(sourcePath);
        }
    }

    @Deprecated
    public String getTags() {
        if (jsonlayout.getTags() != null) {
            Iterator<String> i = jsonlayout.getTags().iterator();
            StringBuilder sb = new StringBuilder();
            while (i.hasNext()) {
                sb.append(i.next());
                if (i.hasNext()) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }
        return null;
    }

    @Deprecated
    public void setTags(String tags) {
        if (tags != null) {
            String[] atags = tags.split(",");
            jsonlayout.setTags(Arrays.asList(atags));
        }
    }

    @Deprecated
    public String getType() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getType();
        }
        return jsonlayout.getType();
    }

    @Deprecated
    public void setType(String type) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setType(type);
        } else {
            jsonlayout.setType(type);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    @Deprecated
    public void setMdc(boolean flag) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setProperties(flag);
        } else {
            jsonlayout.setProperties(flag);
        }
    }

    @Deprecated
    public boolean getMdc() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getProperties();
        }
        return jsonlayout.getProperties();
    }

    @Deprecated
    public void setLocation(boolean flag) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setLocationInfo(flag);
        } else {
            jsonlayout.setLocationInfo(flag);
        }
    }

    @Deprecated
    public boolean getLocation() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getLocationInfo();
        }
        return jsonlayout.getLocationInfo();
    }

    @Deprecated
    public void setCallerStackIndex(int index) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).setCallerStackIdx(index);
        } else {
            jsonlayout.setCallerStackIdx(index);
        }
    }

    @Deprecated
    public int getCallerStackIndex() {
        if(layout instanceof AuditLogLayout) {
            return ((AuditLogLayout) layout).getCallerStackIdx();
        }
        return jsonlayout.getCallerStackIdx();
    }

    @Deprecated
    public void addAdditionalField(AdditionalField p) {
        if(layout instanceof AuditLogLayout) {
            ((AuditLogLayout) layout).addAdditionalField(p);
        } else {
            jsonlayout.addAdditionalField(p);
        }
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public int getTimeBetweenEvictionRuns() {
        return timeBetweenEvictionRuns;
    }

    public void setTimeBetweenEvictionRuns(int timeBetweenEvictionRuns) {
        this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
    }

    public int getMinEvictableIdleTime() {
        return minEvictableIdleTime;
    }

    public void setMinEvictableIdleTime(int minEvictableIdleTime) {
        this.minEvictableIdleTime = minEvictableIdleTime;
    }

    @Override
    public void start() {
        super.start();
        GenericObjectPoolConfig<redis.clients.jedis.Jedis> config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        config.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRuns));
        config.setMinEvictableIdleTime(Duration.ofMillis(minEvictableIdleTime));
        pool = new JedisPool(config, host, port, timeout, password, database);
    }

    @Override
    public void stop() {
        super.stop();
        pool.destroy();
    }

}

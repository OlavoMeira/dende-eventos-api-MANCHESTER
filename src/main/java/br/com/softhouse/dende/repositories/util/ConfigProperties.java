package br.com.softhouse.dende.repositories.util;

import br.com.dende.softhouse.annotations.Value;
import br.com.dende.softhouse.annotations.Component;

@Component
public class ConfigProperties {

    @Value(key = "datasource.url")
    private String url;

    @Value(key = "datasource.username")
    private String username;

    @Value(key = "datasource.password")
    private String password;

    @Value(key = "datasource.driver-class-name")
    private String driverClassName;

    @Value(key = "datasource.hikari.maximum-pool-size")
    private String maximumPoolSize;

    @Value(key = "datasource.hikari.minimum-idle")
    private String minimumIdle;

    @Value(key = "datasource.hikari.connection-timeout")
    private String connectionTimeout;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getMaximumPoolSize() {
        try {
            return Integer.parseInt(maximumPoolSize);
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public int getMinimumIdle() {
        try {
            return Integer.parseInt(minimumIdle);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public long getConnectionTimeout() {
        try {
            return Long.parseLong(connectionTimeout);
        } catch (NumberFormatException e) {
            return 30000L;
        }
    }
}

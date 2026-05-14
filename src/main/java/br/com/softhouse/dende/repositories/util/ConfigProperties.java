package br.com.softhouse.dende.repositories.util;

import br.com.dende.softhouse.annotations.Value;
import br.com.dende.softhouse.annotations.Component;

@Component
public class ConfigProperties {

    @Value("datasource.url")
    private String url;

    @Value("datasource.username")
    private String username;

    @Value("datasource.password")
    private String password;

    @Value("datasource.driver-class-name")
    private String driverClassName;

    @Value("datasource.hikari.maximum-pool-size")
    private String maximumPoolSize;

    @Value("datasource.hikari.minimum-idle")
    private String minimumIdle;

    @Value("datasource.hikari.connection-timeout")
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

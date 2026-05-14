package br.com.softhouse.dende.repositories.util;

import br.com.dende.softhouse.annotations.Component;
import br.com.softhouse.dende.exceptions.DatabaseConnectionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionPool {

    private static ConnectionPool instance;
    private final HikariDataSource dataSource;

    public ConnectionPool(ConfigProperties config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setPoolName("DendeEventosPool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public static synchronized ConnectionPool getInstance(ConfigProperties config) {
        if (instance == null) {
            instance = new ConnectionPool(config);
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Erro ao obter conexão com o banco de dados: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean isRunning() {
        return dataSource != null && !dataSource.isClosed();
    }
}

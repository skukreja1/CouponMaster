package com.coupon.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);

        try {
            URI dbUri = new URI(databaseUrl);
            
            String username = null;
            String password = null;
            
            if (dbUri.getUserInfo() != null) {
                String[] userInfo = dbUri.getUserInfo().split(":");
                username = userInfo[0];
                if (userInfo.length > 1) {
                    password = userInfo[1];
                }
            }
            
            int port = dbUri.getPort();
            if (port == -1) {
                port = 5432;
            }
            
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();
            
            String query = dbUri.getQuery();
            if (query != null && !query.isEmpty()) {
                jdbcUrl += "?" + query;
            }
            
            dataSource.setJdbcUrl(jdbcUrl);
            
            if (username != null) {
                dataSource.setUsername(username);
            }
            if (password != null) {
                dataSource.setPassword(password);
            }
            
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid DATABASE_URL format", e);
        }
        
        return dataSource;
    }
}

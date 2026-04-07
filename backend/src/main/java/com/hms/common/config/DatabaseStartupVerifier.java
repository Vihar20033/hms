package com.hms.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseStartupVerifier implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;
    private final WebServerApplicationContext webServerApplicationContext;

    private static final String DEFAULT_JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Value("${spring.jpa.hibernate.ddl-auto:unknown}")
    private String ddlAuto;

    @Value("${hms.jwt.secret:}")
    private String jwtSecret;

    @Value("${hms.frontend.api-url:unknown}")
    private String frontendApiUrl;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        validateProductionSafety();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String activeSchema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()",
                    Integer.class
            );

            log.info("HMS Backend running from {}", Path.of("").toAbsolutePath().normalize());
            log.info("HMS startup verification: appName={}, port={}, frontendApiUrl={}, dbUrl={}, dbUser={}, activeSchema={}, ddlAuto={}, profiles={}, tableCount={}",
                    environment.getProperty("spring.application.name", "unknown"),
                    webServerApplicationContext.getWebServer().getPort(),
                    frontendApiUrl,
                    metaData.getURL(),
                    metaData.getUserName(),
                    activeSchema,
                    ddlAuto,
                    Arrays.toString(environment.getActiveProfiles()),
                    tableCount);

            if (tableCount != null && tableCount == 0 && !"none".equalsIgnoreCase(ddlAuto)) {
                log.warn("Database schema '{}' currently has no tables. If this is unexpected, verify HMS_DB_URL and refresh MySQL Workbench schemas.", activeSchema);
            }
        }
    }

    private void validateProductionSafety() {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));

        if (!production) {
            return;
        }

        if ("update".equalsIgnoreCase(ddlAuto) || "create".equalsIgnoreCase(ddlAuto) || "create-drop".equalsIgnoreCase(ddlAuto)) {
            throw new IllegalStateException("Production profile cannot start with spring.jpa.hibernate.ddl-auto=" + ddlAuto);
        }

        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("Production profile cannot start with the default JWT secret. Set JWT_SECRET_KEY.");
        }
    }
}

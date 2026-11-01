package org;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

public class ConversionRepository {

    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(2);
    private final ConnectionProvider connectionProvider;
    private final int maxAttempts;
    private final Duration retryDelay;

    public ConversionRepository() {
        String host = environment("DB_HOST", "localhost");
        String port = environment("DB_PORT", "3306");
        String database = environment("DB_NAME", "temperature_converter");
        String url = "jdbc:mariadb://" + host + ":" + port + "/" + database
                + "?sslMode=disable&useServerPrepStmts=true";
        String username = environment("DB_USER", "root");
        String password = environment("DB_PASSWORD", "");
        connectionProvider = () -> DriverManager.getConnection(url, username, password);
        maxAttempts = DEFAULT_MAX_ATTEMPTS;
        retryDelay = DEFAULT_RETRY_DELAY;
    }

    ConversionRepository(ConnectionProvider connectionProvider) {
        this(connectionProvider, DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    ConversionRepository(ConnectionProvider connectionProvider, int maxAttempts,
                         Duration retryDelay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        if (retryDelay.isNegative()) {
            throw new IllegalArgumentException("retryDelay cannot be negative");
        }
        this.connectionProvider = connectionProvider;
        this.maxAttempts = maxAttempts;
        this.retryDelay = retryDelay;
    }

    public void saveFahrenheitToCelsius(double fahrenheit, double celsius, String languageCode)
            throws SQLException {
        save("INSERT INTO fahrenheit_to_celsius "
                + "(fahrenheit, celsius, language_code) VALUES (?, ?, ?)",
                fahrenheit, celsius, languageCode);
    }

    public void saveCelsiusToFahrenheit(double celsius, double fahrenheit, String languageCode)
            throws SQLException {
        save("INSERT INTO celsius_to_fahrenheit "
                + "(celsius, fahrenheit, language_code) VALUES (?, ?, ?)",
                celsius, fahrenheit, languageCode);
    }

    public void saveKelvinToCelsius(double kelvin, double celsius, String languageCode)
            throws SQLException {
        save("INSERT INTO kelvin_to_celsius "
                + "(kelvin, celsius, language_code) VALUES (?, ?, ?)",
                kelvin, celsius, languageCode);
    }

    private void save(String sql, double input, double result, String languageCode)
            throws SQLException {
        SQLException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = connectionProvider.open();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, input);
                statement.setDouble(2, result);
                statement.setString(3, languageCode);
                statement.executeUpdate();
                return;
            } catch (SQLException exception) {
                lastFailure = exception;
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Database connection retry was interrupted",
                                interruptedException);
                    }
                }
            }
        }
        throw lastFailure;
    }

    @FunctionalInterface
    interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

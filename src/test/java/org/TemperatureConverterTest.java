package org;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            Locale.forLanguageTag("en-EN"),
            Locale.forLanguageTag("fi-FI"),
            Locale.forLanguageTag("ja-JP"),
            Locale.forLanguageTag("ko-KR"),
            Locale.forLanguageTag("sv-SE"),
            Locale.forLanguageTag("ar-SA")
    );

    private static final List<String> GUI_MESSAGE_KEYS = List.of(
            "app_title",
            "language",
            "conversion",
            "temperature",
            "temperature_prompt",
            "fahrenheit_to_celsius",
            "celsius_to_fahrenheit",
            "kelvin_to_celsius",
            "convert",
            "result",
            "initial_result",
            "extreme_warning",
            "invalid_temperature",
            "invalid_temperature_header",
            "invalid_temperature_content",
            "database_save_error"
    );

    @Test
    void fahrenheitToCelsius() {
        TemperatureConverter converter = new TemperatureConverter();
        assertEquals(0, converter.fahrenheitToCelsius(32), 0.01);
        assertEquals(100, converter.fahrenheitToCelsius(212), 0.01);
        assertEquals(-40, converter.fahrenheitToCelsius(-40), 0.01);
    }

    @Test
    void celsiusToFahrenheit() {
        TemperatureConverter converter = new TemperatureConverter();
        assertEquals(32, converter.celsiusToFahrenheit(0), 0.01);
        assertEquals(212, converter.celsiusToFahrenheit(100), 0.01);
        assertEquals(-40, converter.celsiusToFahrenheit(-40), 0.01);
    }

    @Test
    void isExtremeTemperature() {
        TemperatureConverter converter = new TemperatureConverter();
        assertTrue(converter.isExtremeTemperature(-50));
        assertTrue(converter.isExtremeTemperature(60));
        assertFalse(converter.isExtremeTemperature(0));
        assertFalse(converter.isExtremeTemperature(50));
    }

    @Test
    void kelvinToCelsius() {
        TemperatureConverter converter = new TemperatureConverter();
        assertEquals(0, converter.kelvinToCelsius(273.15), 0.01);
        assertEquals(-273.15, converter.kelvinToCelsius(0), 0.01);
        assertEquals(100, converter.kelvinToCelsius(373.15), 0.01);
    }

    @Test
    void commandLineMainConvertsFahrenheitInput() {
        String output = runCommandLine("1\n32\n");
        assertTrue(output.contains("0.0"));
    }

    @Test
    void commandLineMainUsesTheSecondLanguageBranch() {
        String output = runCommandLine("2\n212\n");
        assertTrue(output.contains("100.0"));
    }

    @Test
    void localizedGuiMessagesExistForEverySupportedLanguage() {
        for (Locale locale : SUPPORTED_LOCALES) {
            ResourceBundle bundle = ResourceBundle.getBundle("Messages_bundle", locale);

            for (String key : GUI_MESSAGE_KEYS) {
                assertTrue(bundle.containsKey(key),
                        () -> "Missing key '" + key + "' for locale " + locale);
                assertFalse(bundle.getString(key).isBlank(),
                        () -> "Blank key '" + key + "' for locale " + locale);
            }
        }
    }

    @Test
    void supportedLocalesResolveToTheirLanguageBundles() {
        assertEquals("en", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("en-EN")).getLocale().getLanguage());
        assertEquals("fi", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("fi-FI")).getLocale().getLanguage());
        assertEquals("ja", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("ja-JP")).getLocale().getLanguage());
        assertEquals("ko", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("ko-KR")).getLocale().getLanguage());
        assertEquals("sv", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("sv-SE")).getLocale().getLanguage());
        assertEquals("ar", ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("ar-SA")).getLocale().getLanguage());
    }

    @Test
    void savesEachConversionToItsMatchingTable() throws Exception {
        assertConversionSaved(
                (repository, language) -> repository.saveFahrenheitToCelsius(32, 0, language),
                "fahrenheit_to_celsius", 32, 0, "ja");
        assertConversionSaved(
                (repository, language) -> repository.saveCelsiusToFahrenheit(100, 212, language),
                "celsius_to_fahrenheit", 100, 212, "ko");
        assertConversionSaved(
                (repository, language) -> repository.saveKelvinToCelsius(273.15, 0, language),
                "kelvin_to_celsius", 273.15, 0, "ar");
    }

    @Test
    void retriesDatabaseConnectionAfterFailure() throws Exception {
        int[] attempts = {0};
        ConversionRepository repository = new ConversionRepository(
                () -> {
                    attempts[0]++;
                    if (attempts[0] == 1) {
                        throw new SQLException("database is starting");
                    }
                    return createConnection(new ArrayList<>(), new ArrayList<>());
                },
                2,
                Duration.ZERO);

        assertDoesNotThrow(() ->
                repository.saveFahrenheitToCelsius(32, 0, "en"));
        assertEquals(2, attempts[0]);
    }

    @Test
    void reportsTheLastDatabaseFailureAfterRetries() {
        SQLException failure = new SQLException("database unavailable");
        ConversionRepository repository = new ConversionRepository(
                () -> {
                    throw failure;
                },
                3,
                Duration.ZERO);

        SQLException thrown = assertThrows(SQLException.class,
                () -> repository.saveKelvinToCelsius(273.15, 0, "en"));
        assertSame(failure, thrown);
    }

    @Test
    void rejectsInvalidRetryConfiguration() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ConversionRepository(() -> null, 0, Duration.ZERO)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ConversionRepository(() -> null, 1, Duration.ofSeconds(-1)))
        );
    }

    @Test
    void defaultRepositoryCanBeConstructedFromEnvironmentDefaults() {
        assertDoesNotThrow(() -> new ConversionRepository());
    }

    @Test
    void interruptedRetryReportsAnSQLException() {
        Thread.currentThread().interrupt();
        try {
            ConversionRepository repository = new ConversionRepository(
                    () -> {
                        throw new SQLException("database unavailable");
                    },
                    2,
                    Duration.ofSeconds(2));

            SQLException thrown = assertThrows(SQLException.class,
                    () -> repository.saveFahrenheitToCelsius(32, 0, "en"));
            assertTrue(thrown.getMessage().contains("retry was interrupted"));
        } finally {
            Thread.interrupted();
        }
    }

    private void assertConversionSaved(ConversionAction action, String table,
                                       double input, double result, String language)
            throws Exception {
        List<Object> parameters = new ArrayList<>();
        List<String> sql = new ArrayList<>();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setDouble" -> parameters.add(args[1]);
                        case "setString" -> parameters.add(args[1]);
                        case "executeUpdate" -> {
                            return 1;
                        }
                        default -> {
                        }
                    }
                    return null;
                });
        Connection connection = createConnection(sql, parameters, statement);

        action.run(new ConversionRepository(() -> connection), language);

        assertTrue(sql.getFirst().contains("INSERT INTO " + table));
        assertEquals(List.of(input, result, language), parameters);
    }

    private Connection createConnection(List<String> sql, List<Object> parameters) {
        return createConnection(sql, parameters, null);
    }

    private Connection createConnection(List<String> sql, List<Object> parameters,
                                        PreparedStatement preparedStatement) {
        PreparedStatement statement = preparedStatement == null
                ? (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> method.getName().equals("executeUpdate") ? 1 : null)
                : preparedStatement;
        return (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("prepareStatement")) {
                        sql.add((String) args[0]);
                        return statement;
                    }
                    return null;
                });
    }

    private String runCommandLine(String input) {
        java.io.InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            TemperatureConverter.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
    }

    @FunctionalInterface
    private interface ConversionAction {
        void run(ConversionRepository repository, String language) throws Exception;
    }
}
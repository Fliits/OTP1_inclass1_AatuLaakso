package org;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
            "invalid_temperature_content"
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
}
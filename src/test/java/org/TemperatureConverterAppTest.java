package org;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemperatureConverterAppTest {

    @Test
    void mapsLanguageChoicesToSupportedLocales() {
        List<String> expectedLanguages = List.of("en", "fi", "ja", "ko", "sv", "ar");

        for (int index = 0; index < expectedLanguages.size(); index++) {
            Locale locale = TemperatureConverterApp.localeForLanguageIndex(index);
            assertEquals(expectedLanguages.get(index), locale.getLanguage());
        }
    }

    @Test
    void mapsLanguageChoicesToExpectedRegions() {
        List<String> expectedRegions = List.of("EN", "FI", "JP", "KR", "SE", "SA");

        for (int index = 0; index < expectedRegions.size(); index++) {
            assertEquals(expectedRegions.get(index),
                    TemperatureConverterApp.localeForLanguageIndex(index).getCountry());
        }
    }

    @Test
    void fallsBackToEnglishForUnknownLanguageChoice() {
        assertEquals("en", TemperatureConverterApp.localeForLanguageIndex(-1).getLanguage());
        assertEquals("en", TemperatureConverterApp.localeForLanguageIndex(99).getLanguage());
        assertEquals("en", TemperatureConverterApp.languageCodeForIndex(-1));
        assertEquals("en", TemperatureConverterApp.languageCodeForIndex(99));
    }

    @Test
    void onlyArabicUsesRightToLeftOrientation() {
        assertTrue(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ar-SA")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("en-EN")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("fi-FI")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ja-JP")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ko-KR")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("sv-SE")));
    }

    @Test
    void mapsEveryLanguageChoiceToItsDatabaseCode() {
        assertEquals("en", TemperatureConverterApp.languageCodeForIndex(0));
        assertEquals("fi", TemperatureConverterApp.languageCodeForIndex(1));
        assertEquals("ja", TemperatureConverterApp.languageCodeForIndex(2));
        assertEquals("ko", TemperatureConverterApp.languageCodeForIndex(3));
        assertEquals("sv", TemperatureConverterApp.languageCodeForIndex(4));
        assertEquals("ar", TemperatureConverterApp.languageCodeForIndex(5));
    }

    @Test
    void selectedLanguageCodeUsesTheSelectedLanguageIndex() {
        List<String> expectedCodes = List.of("en", "fi", "ja", "ko", "sv", "ar");

        for (int index = 0; index < expectedCodes.size(); index++) {
            assertEquals(expectedCodes.get(index),
                    TemperatureConverterApp.selectedLanguageCode(index));
        }
    }

    @Test
    void calculatesEverySupportedConversion() {
        TemperatureConverter converter = new TemperatureConverter();

        TemperatureConverterApp.ConversionResult fahrenheit =
                TemperatureConverterApp.calculateConversion(0, 212, converter);
        assertEquals(100.0, fahrenheit.value(), 0.0001);
        assertEquals("°C", fahrenheit.unit());

        TemperatureConverterApp.ConversionResult celsius =
                TemperatureConverterApp.calculateConversion(1, 100, converter);
        assertEquals(212.0, celsius.value(), 0.0001);
        assertEquals("°F", celsius.unit());

        TemperatureConverterApp.ConversionResult kelvin =
                TemperatureConverterApp.calculateConversion(2, 273.15, converter);
        assertEquals(0.0, kelvin.value(), 0.0001);
        assertEquals("°C", kelvin.unit());
    }

    @Test
    void rejectsUnknownConversionChoice() {
        assertThrows(IllegalArgumentException.class,
                () -> TemperatureConverterApp.calculateConversion(-1, 0,
                        new TemperatureConverter()));
        assertThrows(IllegalArgumentException.class,
                () -> TemperatureConverterApp.calculateConversion(3, 0,
                        new TemperatureConverter()));
    }

    @Test
    void formatsConversionResultWithTwoDecimalPlaces() {
        assertEquals("Result: 100.00 °C",
                TemperatureConverterApp.formatResult("Result:", 100, "°C"));
        assertEquals("Result: -2.35 °F",
                TemperatureConverterApp.formatResult("Result:", -2.345, "°F"));
    }

    @Test
    void convertsResultToCelsiusOnlyWhenResultIsFahrenheit() {
        TemperatureConverter converter = new TemperatureConverter();

        assertEquals(50.0, TemperatureConverterApp.celsiusValue(50, "°C", converter));
        assertEquals(100.0, TemperatureConverterApp.celsiusValue(212, "°F", converter));
    }

    @Test
    void reportsExtremeAndNormalTemperatures() {
        ResourceBundle messages = ResourceBundle.getBundle(
                "Messages_bundle", Locale.forLanguageTag("en-US"));
        TemperatureConverter converter = new TemperatureConverter();

        assertEquals(messages.getString("extreme_warning"),
                TemperatureConverterApp.extremeStatus(messages, 100, "°C", converter));
        assertEquals(messages.getString("extreme_warning"),
                TemperatureConverterApp.extremeStatus(messages, -40.1, "°C", converter));
        assertEquals(messages.getString("extreme_warning"),
                TemperatureConverterApp.extremeStatus(messages, 212, "°F", converter));
        assertEquals("",
                TemperatureConverterApp.extremeStatus(messages, 20, "°C", converter));
    }
}

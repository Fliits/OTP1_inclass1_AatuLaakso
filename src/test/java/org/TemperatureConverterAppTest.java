package org;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void fallsBackToEnglishForUnknownLanguageChoice() {
        assertEquals("en", TemperatureConverterApp.localeForLanguageIndex(-1).getLanguage());
        assertEquals("en", TemperatureConverterApp.localeForLanguageIndex(99).getLanguage());
        assertEquals("en", TemperatureConverterApp.languageCodeForIndex(-1));
        assertEquals("en", TemperatureConverterApp.languageCodeForIndex(99));
    }

    @Test
    void onlyArabicUsesRightToLeftOrientation() {
        assertTrue(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ar-SA")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ja-JP")));
        assertFalse(TemperatureConverterApp.isRightToLeft(Locale.forLanguageTag("ko-KR")));
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
}

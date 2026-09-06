package org;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {

    @org.junit.jupiter.api.Test
    void fahrenheitToCelsius() {
        TemperatureConverter converter = new TemperatureConverter();
        assertEquals(0, converter.fahrenheitToCelsius(32), 0.01);
        assertEquals(100, converter.fahrenheitToCelsius(212), 0.01);
        assertEquals(-40, converter.fahrenheitToCelsius(-40), 0.01);
    }

    @org.junit.jupiter.api.Test
    void celsiusToFahrenheit() {
        TemperatureConverter converter = new TemperatureConverter();
        assertEquals(32, converter.celsiusToFahrenheit(0), 0.01);
        assertEquals(212, converter.celsiusToFahrenheit(100), 0.01);
        assertEquals(-40, converter.celsiusToFahrenheit(-40), 0.01);
    }

    @org.junit.jupiter.api.Test
    void isExtremeTemperature() {
        TemperatureConverter converter = new TemperatureConverter();
        assertTrue(converter.isExtremeTemperature(-50));
        assertTrue(converter.isExtremeTemperature(60));
        assertFalse(converter.isExtremeTemperature(0));
        assertFalse(converter.isExtremeTemperature(50));
    }

}
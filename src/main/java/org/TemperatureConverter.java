package org;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class TemperatureConverter {

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Locale locale = Locale.getDefault();
        System.out.println("Select language (en/kr): ");
        System.out.print("Enter 1 for English, 2 for Korean: ");
        int lang = sc.nextInt();
        if (lang == 1) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.FRENCH;
        }

        ResourceBundle rb = ResourceBundle.getBundle("Messages_bundle", locale);
        System.out.println(rb.getString("welcome_message"));

        TemperatureConverter converter = new TemperatureConverter();

        System.out.println(rb.getString("prompt_temperature_fr"));
        double temperature = sc.nextDouble();
        double celsius = converter.fahrenheitToCelsius(temperature);
        System.out.println(rb.getString("result_message_c") + celsius);
    }

    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5/9;
    }

    public double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }

    public boolean isExtremeTemperature (double celsius) {
        return celsius < -40 || celsius > 50;
    }

    public double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }
}

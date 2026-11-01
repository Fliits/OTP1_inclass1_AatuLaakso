package org;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.NodeOrientation;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class TemperatureConverterApp extends Application {

    private final TemperatureConverter converter = new TemperatureConverter();
    private final ConversionRepository conversionRepository = new ConversionRepository();
    private final TextField inputField = new TextField();
    private final ComboBox<String> conversionChoice = new ComboBox<>();
    private final ComboBox<String> languageChoice = new ComboBox<>();
    private final Button convertButton = new Button();
    private final Label conversionLabel = new Label();
    private final Label temperatureLabel = new Label();
    private final Label languageLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label titleLabel = new Label();
    private final VBox root = new VBox(16);
    private ResourceBundle messages;

    @Override
    public void start(Stage stage) {
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        languageChoice.getItems().addAll("English", "Suomi", "日本語", "한국어", "Svenska", "العربية");
        languageChoice.getSelectionModel().select("English");
        languageChoice.setOnAction(event -> updateLanguage(stage));

        conversionChoice.getSelectionModel().selectFirst();

        inputField.setPromptText("Temperature");
        inputField.setOnAction(event -> convert());

        convertButton.setDefaultButton(true);
        convertButton.setOnAction(event -> convert());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(languageLabel, 0, 0);
        form.add(languageChoice, 1, 0);
        form.add(conversionLabel, 0, 1);
        form.add(conversionChoice, 1, 1);
        form.add(temperatureLabel, 0, 2);
        form.add(inputField, 1, 2);
        form.add(convertButton, 1, 3);

        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        statusLabel.setStyle("-fx-text-fill: #b45309;");

        root.getChildren().addAll(titleLabel, form, resultLabel, statusLabel);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_LEFT);

        messages = ResourceBundle.getBundle("Messages_bundle", new Locale("en", "EN"));
        updateLanguage(stage);
        stage.setScene(new Scene(root, 500, 350));
        stage.show();
    }

    private void updateLanguage(Stage stage) {
        Locale locale = localeForLanguageIndex(
                languageChoice.getSelectionModel().getSelectedIndex());
        messages = ResourceBundle.getBundle("Messages_bundle", locale);
        root.setNodeOrientation(isRightToLeft(locale)
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);
        stage.setTitle(messages.getString("app_title"));
        titleLabel.setText(messages.getString("app_title"));
        languageLabel.setText(messages.getString("language"));
        conversionLabel.setText(messages.getString("conversion"));
        temperatureLabel.setText(messages.getString("temperature"));
        convertButton.setText(messages.getString("convert"));
        inputField.setPromptText(messages.getString("temperature_prompt"));
        conversionChoice.getItems().setAll(
                messages.getString("fahrenheit_to_celsius"),
                messages.getString("celsius_to_fahrenheit"),
                messages.getString("kelvin_to_celsius")
        );
        conversionChoice.getSelectionModel().selectFirst();
        resultLabel.setText(messages.getString("initial_result"));
        statusLabel.setText("");
    }

    private void convert() {
        try {
            double input = Double.parseDouble(inputField.getText().trim());
            double result;
            String unit;

            switch (conversionChoice.getSelectionModel().getSelectedIndex()) {
                case 0 -> {
                    result = converter.fahrenheitToCelsius(input);
                    unit = "°C";
                    conversionRepository.saveFahrenheitToCelsius(input, result, selectedLanguageCode());
                }
                case 1 -> {
                    result = converter.celsiusToFahrenheit(input);
                    unit = "°F";
                    conversionRepository.saveCelsiusToFahrenheit(input, result, selectedLanguageCode());
                }
                case 2 -> {
                    result = converter.kelvinToCelsius(input);
                    unit = "°C";
                    conversionRepository.saveKelvinToCelsius(input, result, selectedLanguageCode());
                }
                default -> throw new IllegalStateException("No conversion selected");
            }

            resultLabel.setText(String.format(Locale.ROOT, "%s %.2f %s",
                    messages.getString("result"), result, unit));
            double celsius = unit.equals("°C")
                    ? result
                    : converter.fahrenheitToCelsius(result);
            statusLabel.setText(converter.isExtremeTemperature(celsius)
                    ? messages.getString("extreme_warning")
                    : "");
        } catch (NumberFormatException exception) {
            showInputError();
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    static Locale localeForLanguageIndex(int languageIndex) {
        return switch (languageIndex) {
            case 1 -> Locale.forLanguageTag("fi-FI");
            case 2 -> Locale.forLanguageTag("ja-JP");
            case 3 -> Locale.forLanguageTag("ko-KR");
            case 4 -> Locale.forLanguageTag("sv-SE");
            case 5 -> Locale.forLanguageTag("ar-SA");
            default -> new Locale("en", "EN");
        };
    }

    static boolean isRightToLeft(Locale locale) {
        return locale.getLanguage().equals("ar");
    }

    static String languageCodeForIndex(int languageIndex) {
        return switch (languageIndex) {
            case 1 -> "fi";
            case 2 -> "ja";
            case 3 -> "ko";
            case 4 -> "sv";
            case 5 -> "ar";
            default -> "en";
        };
    }

    String selectedLanguageCode() {
        return languageCodeForIndex(languageChoice.getSelectionModel().getSelectedIndex());
    }

    private void showInputError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.getString("invalid_temperature"));
        alert.setHeaderText(messages.getString("invalid_temperature_header"));
        alert.setContentText(messages.getString("invalid_temperature_content"));
        alert.showAndWait();
    }

    private void showDatabaseError(SQLException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.getString("database_save_error"));
        alert.setHeaderText(messages.getString("database_save_error"));
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

-- Temperature Converter database schema.
CREATE DATABASE IF NOT EXISTS temperature_converter
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE temperature_converter;

-- Languages supported by the JavaFX application.
CREATE TABLE IF NOT EXISTS language (
    code        CHAR(2) PRIMARY KEY,
    name        VARCHAR(40) NOT NULL
) CHARACTER SET utf8mb4;

INSERT IGNORE INTO language (code, name) VALUES
    ('en', 'English'),
    ('fi', 'Finnish'),
    ('ja', 'Japanese'),
    ('ko', 'Korean'),
    ('sv', 'Swedish'),
    ('ar', 'Arabic');

-- Fahrenheit to Celsius conversion history.
CREATE TABLE IF NOT EXISTS fahrenheit_to_celsius (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    fahrenheit          DECIMAL(10, 4) NOT NULL,
    celsius             DECIMAL(10, 4) NOT NULL,
    language_code       CHAR(2) NOT NULL DEFAULT 'en',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (language_code) REFERENCES language(code)
) CHARACTER SET utf8mb4;

-- Celsius to Fahrenheit conversion history.
CREATE TABLE IF NOT EXISTS celsius_to_fahrenheit (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    celsius             DECIMAL(10, 4) NOT NULL,
    fahrenheit          DECIMAL(10, 4) NOT NULL,
    language_code       CHAR(2) NOT NULL DEFAULT 'en',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (language_code) REFERENCES language(code)
) CHARACTER SET utf8mb4;

-- Kelvin to Celsius conversion history.
CREATE TABLE IF NOT EXISTS kelvin_to_celsius (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    kelvin              DECIMAL(10, 4) NOT NULL,
    celsius             DECIMAL(10, 4) NOT NULL,
    language_code       CHAR(2) NOT NULL DEFAULT 'en',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (language_code) REFERENCES language(code)
) CHARACTER SET utf8mb4;

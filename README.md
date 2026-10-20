# Temperature Converter

## 1. Description

This project is a small Java library for converting temperatures and identifying extreme Celsius temperatures. The `TemperatureConverter` class supports Fahrenheit-to-Celsius, Celsius-to-Fahrenheit, and Kelvin-to-Celsius conversions. It also reports whether a Celsius value is below -40 °C or above 50 °C.

## 2. Technologies and Dependencies

- Java 25
- Apache Maven
- JUnit Jupiter 5.14.0 for automated tests
- JaCoCo 0.8.15 for code coverage
- Jenkins for continuous integration
- Docker Hub publishing through the Jenkins pipeline

## 3. Design Approach and Implementation

The implementation uses a focused, stateless `TemperatureConverter` class. Each public method accepts a numeric temperature and returns either the converted `double` value or a `boolean` result for the extreme-temperature check.

The conversion formulas are:

- Fahrenheit to Celsius: `(fahrenheit - 32) * 5 / 9`
- Celsius to Fahrenheit: `(celsius * 9 / 5) + 32`
- Kelvin to Celsius: `kelvin - 273.15`

The Maven project follows the standard layout: production code is in `src/main/java` and tests are in `src/test/java`. The Jenkins pipeline checks out the project, builds it, runs the tests, generates coverage reports, publishes test and coverage results, and builds and pushes a Docker image.

## 4. Testing and Quality Assurance

JUnit tests cover the supported conversion methods and the extreme-temperature boundary behavior. The test cases include freezing and boiling points, the -40-degree equality point, absolute-zero conversion, and values on both sides of the extreme-temperature limits.

Run the test suite with Maven:

```bash
mvn test
```

JaCoCo is configured to collect execution data during testing and generate a coverage report. A full build can be used in the Jenkins pipeline with `mvn clean install`, followed by `mvn jacoco:report` when a standalone coverage report is required.

## 5. Set-up and How To Run

Install Java 25 and Maven, then run the following commands from the project root:

```bash
mvn clean install
mvn test
```

The project is a library without a command-line entry point, so its functionality is exercised through Java code and the automated tests. To use it in Java code:

```java
TemperatureConverter converter = new TemperatureConverter();
double celsius = converter.fahrenheitToCelsius(32);
double fahrenheit = converter.celsiusToFahrenheit(100);
boolean extreme = converter.isExtremeTemperature(60);
```

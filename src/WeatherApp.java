import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherApp extends Application {

    private TextField cityInput;
    private Label temperatureLabel;
    private Label humidityLabel;
    private Label windSpeedLabel;
    private Label conditionLabel;
    private ImageView weatherIcon;
    private ListView<String> historyListView;
    private List<String> searchHistory = new ArrayList<>();
    private boolean isCelsius = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        cityInput = new TextField();
        cityInput.setPromptText("Enter city name");
    
        Button searchButton = new Button("Get Weather");
        searchButton.setOnAction(e -> fetchWeatherData());
    
        temperatureLabel = new Label("Temperature: ");
        humidityLabel = new Label("Humidity: ");
        windSpeedLabel = new Label("Wind Speed: ");
        conditionLabel = new Label("Condition: ");
        weatherIcon = new ImageView();
    
        Button unitButton = new Button("Switch to Fahrenheit");
        unitButton.setOnAction(e -> toggleUnits());
    
        historyListView = new ListView<>();
    
        VBox weatherBox = new VBox(10, temperatureLabel, humidityLabel, windSpeedLabel, conditionLabel, weatherIcon);
        weatherBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #ffffff;");
    
        VBox layout = new VBox(10, cityInput, searchButton, weatherBox, unitButton, historyListView);
        layout.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");
    
        Scene scene = new Scene(layout, 300, 500);
    
        primaryStage.setScene(scene);
        primaryStage.setTitle("Weather Information App");
        primaryStage.show();
    }
private void fetchWeatherData() {
    String city = cityInput.getText();
    if (city.isEmpty()) {
        showAlert("Error", "City name cannot be empty.");
        return;
    }

    try {
        String apiKey = "api-key"; // Replace with your API key
        String apiUrl = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s", city, isCelsius ? "metric" : "imperial", apiKey);
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // Set connection timeout
        connection.setReadTimeout(5000); // Set read timeout

        int status = connection.getResponseCode();
        if (status != 200) {
            showAlert("Error", "Failed to fetch weather data. HTTP response code: " + status);
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        parseWeatherData(content.toString());
        addToHistory(city);

    } catch (Exception e) {
        showAlert("Error", "Failed to fetch weather data. Error: " + e.getMessage());
    }
}

    private void parseWeatherData(String responseBody) {
        JSONObject json = new JSONObject(responseBody);
        JSONObject main = json.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");

        JSONObject wind = json.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");

        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
        String condition = weather.getString("description");
        String icon = weather.getString("icon");

        temperatureLabel.setText("Temperature: " + temperature + (isCelsius ? " °C" : " °F"));
        humidityLabel.setText("Humidity: " + humidity + "%");
        windSpeedLabel.setText("Wind Speed: " + windSpeed + (isCelsius ? " m/s" : " mph"));
        conditionLabel.setText("Condition: " + condition);
        weatherIcon.setImage(new Image("http://openweathermap.org/img/wn/" + icon + "@2x.png"));
    }

    private void toggleUnits() {
        isCelsius = !isCelsius;
        fetchWeatherData();
    }

    private void addToHistory(String city) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String historyEntry = String.format("%s: %s", timestamp, city);
        searchHistory.add(historyEntry);
        historyListView.getItems().add(historyEntry);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

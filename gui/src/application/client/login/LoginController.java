package application.client.login;

import application.client.http.HttpApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class LoginController {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 8080;

    @FXML private TextField usernameField;
    @FXML private Button    loginButton;
    @FXML private Label     statusLabel;

    @FXML
    void initialize() {
        // Allow pressing Enter to sign in
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) onLogin();
        });
    }

    @FXML
    void onLogin() {
        String username = usernameField.getText().trim();
        if (username.isBlank()) {
            showError("Please enter a username.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12; -fx-padding: 12 0 0 0;");
        statusLabel.setText("Connecting…");

        new Thread(() -> {
            try {
                HttpApiClient client = new HttpApiClient(SERVER_HOST, SERVER_PORT);
                String error = client.loginError(username);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    if (error != null) {
                        showError(error);
                    } else {
                        openDashboard(username, client);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showError("Cannot reach server at " + SERVER_HOST + ":" + SERVER_PORT);
                });
            }
        }, "login-thread").start();
    }

    private void openDashboard(String username, HttpApiClient client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/application/client/dashboard/Dashboard.fxml"));
            Parent root = loader.load();

            application.client.dashboard.DashboardController dc = loader.getController();
            dc.init(username, client);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("S-Emulator  |  " + username);
            stage.setScene(new Scene(root, 1280, 780));
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setOnCloseRequest(ev -> dc.onClose());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open dashboard: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 12; -fx-padding: 12 0 0 0;");
        statusLabel.setText(msg);
    }
}

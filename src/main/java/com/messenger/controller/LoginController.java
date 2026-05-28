package com.messenger.controller;

import com.messenger.client.Session;
import com.messenger.client.SocketClient;
import com.messenger.database.UserService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {

        loginButton.setOnAction(
                event -> login()
        );

        registerButton.setOnAction(
                event -> openRegisterWindow()
        );
    }

    private void login() {

        String username =
                usernameField.getText();

        String password =
                passwordField.getText();

        boolean success =
                UserService.loginUser(
                        username,
                        password
                );

        if (success) {

            Session.setUsername(username);

            SocketClient.connect(username);

            openChatWindow();

        } else {

            showAlert(
                    "Error",
                    "Invalid username or password"
            );
        }
    }

    private void openChatWindow() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/chat.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            Stage stage =
                    (Stage) loginButton
                            .getScene()
                            .getWindow();

            stage.setOnCloseRequest(event -> {

                SocketClient.disconnect();
            });

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void openRegisterWindow() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/register.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            Stage stage =
                    (Stage) registerButton
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}
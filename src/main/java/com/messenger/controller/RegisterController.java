package com.messenger.controller;

import com.messenger.database.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button createButton;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {

        createButton.setOnAction(event -> register());

        backButton.setOnAction(event -> openLoginWindow());
    }

    private void register() {

        String name =
                nameField.getText();

        String username =
                usernameField.getText();

        String password =
                passwordField.getText();

        boolean success =
                UserService.registerUser(
                        name,
                        username,
                        password
                );

        if (success) {

            showAlert(
                    "Success",
                    "Account created!"
            );

            openLoginWindow();

        } else {

            showAlert(
                    "Error",
                    "User already exists"
            );
        }
    }

    private void openLoginWindow() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            Stage stage =
                    (Stage) backButton
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
package com.messenger.controller;

import com.messenger.client.Session;
import com.messenger.client.SocketClient;
import com.messenger.database.MessageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    private final SocketClient client =
            new SocketClient();

    @FXML
    public void initialize() {

        client.connect("127.0.0.1");

        loadHistory();

        client.listenForMessages(message -> {

            Platform.runLater(() -> {

                chatArea.appendText(
                        message + "\n"
                );

            });

        });

        sendButton.setOnAction(event -> {

            String text =
                    messageField.getText();

            String username =
                    Session.getUsername();

            String fullMessage =
                    username + ": " + text;

            client.sendMessage(fullMessage);

            MessageService.saveMessage(
                    username,
                    text
            );

            messageField.clear();

        });
    }

    private void loadHistory() {

        for (String message :
                MessageService.loadMessages()) {

            chatArea.appendText(
                    message + "\n"
            );
        }
    }
}
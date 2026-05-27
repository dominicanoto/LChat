package com.messenger.controller;

import com.messenger.client.Session;
import com.messenger.database.DialogService;
import com.messenger.database.MessageService;
import com.messenger.database.UserService;
import com.messenger.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChatController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<User> dialogsList;

    @FXML
    private Label nameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label emptyLabel;

    private User selectedUser;

    private int currentDialogId = -1;

    @FXML
    public void initialize() {

        // Empty state

        chatArea.setVisible(false);

        messageField.setVisible(false);

        sendButton.setVisible(false);

        nameLabel.setText("Select a chat");

        usernameLabel.setText("");

        // Search user

        searchField.setOnAction(event -> {

            String username =
                    searchField.getText();

            User user =
                    UserService.findUserByUsername(
                            username
                    );

            dialogsList.getItems().clear();

            if (user != null) {

                dialogsList.getItems().add(user);

                emptyLabel.setVisible(false);

            } else {

                emptyLabel.setVisible(true);
            }
        });

        // Open chat

        dialogsList.setOnMouseClicked(event -> {

            selectedUser =
                    dialogsList.getSelectionModel()
                            .getSelectedItem();

            if (selectedUser != null) {

                openChat(selectedUser);

            }
        });
    }

    private void openChat(User user) {

        nameLabel.setText(
                user.getName()
        );

        usernameLabel.setText(
                "@" + user.getUsername()
        );

        chatArea.setVisible(true);

        messageField.setVisible(true);

        sendButton.setVisible(true);

        currentDialogId =
                DialogService.getOrCreateDialog(
                        Session.getUsername(),
                        user.getUsername()
                );

        loadMessages();

        sendButton.setOnAction(
                event -> sendMessage()
        );
    }

    private void sendMessage() {

        if (selectedUser == null) {
            return;
        }

        String text =
                messageField.getText();

        if (text.isBlank()) {
            return;
        }

        String sender =
                Session.getUsername();

        String receiver =
                selectedUser.getUsername();

        MessageService.saveMessage(
                currentDialogId,
                sender,
                receiver,
                text
        );

        chatArea.appendText(
                sender + ": " + text + "\n"
        );

        messageField.clear();
    }

    private void loadMessages() {

        chatArea.clear();

        for (String message :
                MessageService.loadMessages(
                        currentDialogId
                )) {

            chatArea.appendText(
                    message + "\n"
            );
        }
    }
}
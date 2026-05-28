package com.messenger.controller;

import com.messenger.client.OnlineService;
import com.messenger.client.Session;
import com.messenger.client.SocketClient;

import com.messenger.database.DialogService;
import com.messenger.database.MessageService;
import com.messenger.database.UserService;

import com.messenger.model.User;

import javafx.fxml.FXML;

import javafx.scene.control.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    private Label statusLabel;

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

    private final Map<String, Integer>
            unreadMessages =
            new HashMap<>();

    @FXML
    public void initialize() {

        chatArea.setVisible(false);

        messageField.setVisible(false);

        sendButton.setVisible(false);

        nameLabel.setText(
                "Select a chat"
        );

        usernameLabel.setText("");

        statusLabel.setText("");

        loadDialogs();

        dialogsList.setCellFactory(list ->
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            User user,
                            boolean empty
                    ) {

                        super.updateItem(
                                user,
                                empty
                        );

                        if (empty || user == null) {

                            setText(null);

                        } else {

                            int unread =
                                    unreadMessages
                                            .getOrDefault(
                                                    user.getUsername(),
                                                    0
                                            );

                            boolean online =
                                    OnlineService.isOnline(
                                            user.getUsername()
                                    );

                            String status =
                                    online
                                            ? "🟢 "
                                            : "🔴 ";

                            String text =
                                    status +
                                            user.getName() +
                                            " (@" +
                                            user.getUsername() +
                                            ")";

                            if (unread > 0) {

                                text +=
                                        " [" +
                                                unread +
                                                "]";
                            }

                            setText(text);
                        }
                    }
                });

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

        dialogsList.setOnMouseClicked(event -> {

            selectedUser =
                    dialogsList.getSelectionModel()
                            .getSelectedItem();

            if (selectedUser != null) {

                openChat(selectedUser);

            }
        });

        SocketClient.listen(message -> {

            if (message.startsWith(
                    "SYSTEM_ONLINE:"
            )) {

                String username =
                        message.replace(
                                "SYSTEM_ONLINE:",
                                ""
                        );

                OnlineService.setOnline(
                        username,
                        true
                );

                updateDialogs();

                updateStatus();

                return;
            }

            if (message.startsWith(
                    "SYSTEM_OFFLINE:"
            )) {

                String username =
                        message.replace(
                                "SYSTEM_OFFLINE:",
                                ""
                        );

                OnlineService.setOnline(
                        username,
                        false
                );

                updateDialogs();

                updateStatus();

                return;
            }

            int separator =
                    message.indexOf(":");

            if (separator == -1) {
                return;
            }

            String sender =
                    message.substring(
                            0,
                            separator
                    );

            String text =
                    message.substring(
                            separator + 1
                    );

            User senderUser =
                    UserService.getUser(sender);

            boolean exists = false;

            for (User user :
                    dialogsList.getItems()) {

                if (user.getUsername()
                        .equals(sender)) {

                    exists = true;
                    break;
                }
            }

            if (!exists && senderUser != null) {

                dialogsList.getItems()
                        .add(senderUser);
            }

            int dialogId =
                    DialogService.getOrCreateDialog(
                            Session.getUsername(),
                            sender
                    );

            MessageService.saveMessage(
                    dialogId,
                    sender,
                    Session.getUsername(),
                    text
            );

            if (selectedUser != null &&
                    selectedUser.getUsername()
                            .equals(sender)) {

                chatArea.appendText(
                        sender + ": " +
                                text + "\n"
                );

            } else {

                unreadMessages.put(
                        sender,
                        unreadMessages
                                .getOrDefault(
                                        sender,
                                        0
                                ) + 1
                );

                updateDialogs();
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

        unreadMessages.remove(
                user.getUsername()
        );

        updateDialogs();

        updateStatus();

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

        SocketClient.sendMessage(
                receiver,
                text
        );

        chatArea.appendText(
                sender + ": " +
                        text + "\n"
        );

        messageField.clear();

        loadDialogs();
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

    private void loadDialogs() {

        dialogsList.getItems().clear();

        List<String> dialogs =
                DialogService.loadUserDialogs(
                        Session.getUsername()
                );

        for (String username : dialogs) {

            User user =
                    UserService.getUser(
                            username
                    );

            if (user != null) {

                dialogsList.getItems().add(user);

            }
        }

        emptyLabel.setVisible(
                dialogs.isEmpty()
        );
    }

    private void updateDialogs() {

        dialogsList.refresh();
    }

    private void updateStatus() {

        if (selectedUser == null) {
            return;
        }

        boolean online =
                OnlineService.isOnline(
                        selectedUser.getUsername()
                );

        if (online) {

            statusLabel.setText(
                    "online"
            );

        } else {

            statusLabel.setText(
                    UserService.getLastSeen(
                            selectedUser.getUsername()
                    )
            );
        }
    }
}
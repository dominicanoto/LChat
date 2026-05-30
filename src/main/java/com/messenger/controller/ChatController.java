package com.messenger.controller;

import com.messenger.client.OnlineService;
import com.messenger.client.Session;
import com.messenger.client.SocketClient;
import com.messenger.client.ThemeService;

import com.messenger.database.DialogService;
import com.messenger.database.MessageService;
import com.messenger.database.UserService;

import com.messenger.model.User;
import com.messenger.protocol.XmlProtocol;
import com.messenger.protocol.XmlProtocol.XmlMessage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class ChatController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label currentNameLabel;

    @FXML
    private Label currentUsernameLabel;

    @FXML
    private Button editNameButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button themeButton;

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

    private final ObservableList<User>
            dialogUsers =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        ThemeService.applyTheme(
                rootPane,
                themeButton
        );

        loadCurrentUser();

        chatArea.setVisible(false);

        messageField.setVisible(false);

        sendButton.setVisible(false);

        nameLabel.setText(
                "Select a chat"
        );

        usernameLabel.setText("");

        statusLabel.setText("");

        dialogsList.setItems(dialogUsers);

        loadDialogs();

        editNameButton.setOnAction(
                event -> editName()
        );

        logoutButton.setOnAction(
                event -> logout()
        );

        themeButton.setOnAction(
                event -> ThemeService.toggleTheme(
                        rootPane,
                        themeButton
                )
        );

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

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    performSearch(newValue);
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

            XmlMessage xmlMessage =
                    XmlProtocol.parse(message);

            if ("presence".equals(
                    xmlMessage.type()
            )) {

                String username =
                        xmlMessage.username();

                OnlineService.setOnline(
                        username,
                        "online".equals(
                                xmlMessage.status()
                        )
                );

                updateDialogs();

                updateStatus();

                return;
            }

            if (!"chat".equals(
                    xmlMessage.type()
            )) {
                return;
            }

            String sender =
                    xmlMessage.sender();

            String text =
                    xmlMessage.text();

            User senderUser =
                    UserService.getUser(sender);

            boolean exists = false;

            for (User user :
                    dialogUsers) {

                if (user.getUsername()
                        .equals(sender)) {

                    exists = true;
                    break;
                }
            }

            if (!exists && senderUser != null) {

                dialogUsers.add(senderUser);
            }

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

    private void loadCurrentUser() {

        User user =
                UserService.getUser(
                        Session.getUsername()
                );

        if (user != null) {

            currentNameLabel.setText(
                    user.getName()
            );

            currentUsernameLabel.setText(
                    "@" + user.getUsername()
            );
        }
    }

    private void performSearch(
            String search
    ) {

        if (search == null ||
                search.isBlank()) {

            loadDialogs();

            return;
        }

        User user =
                UserService.findUserByUsername(
                        search
                );

        if (user == null) {

            dialogsList.setItems(
                    FXCollections.observableArrayList()
            );

            return;
        }

        if (user.getUsername().equals(
                Session.getUsername()
        )) {

            dialogsList.setItems(
                    FXCollections.observableArrayList()
            );

            return;
        }

        dialogsList.setItems(
                FXCollections.observableArrayList(
                        user
                )
        );
    }

    private void editName() {

        TextInputDialog dialog =
                new TextInputDialog(
                        currentNameLabel.getText()
                );

        dialog.setTitle("Change name");
        dialog.setHeaderText(null);
        dialog.setContentText("New display name:");

        Optional<String> result =
                dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String newName =
                result.get().trim();

        if (newName.isBlank()) {
            return;
        }

        boolean success =
                UserService.updateName(
                        Session.getUsername(),
                        newName
                );

        if (success) {

            currentNameLabel.setText(newName);
            loadDialogs();
        }
    }

    private void logout() {

        try {

            SocketClient.disconnect();

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            Stage stage =
                    (Stage) logoutButton
                            .getScene()
                            .getWindow();

            stage.setOnCloseRequest(null);
            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();
        }
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

        dialogUsers.clear();

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

                dialogUsers.add(user);
            }
        }

        dialogsList.setItems(dialogUsers);

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

            statusLabel.setStyle(
                    "-fx-text-fill: #34C759;"
            );

        } else {

            statusLabel.setText(
                    UserService.getLastSeen(
                            selectedUser.getUsername()
                    )
            );

            statusLabel.setStyle(
                    "-fx-text-fill: #8e8e93;"
            );
        }
    }
}

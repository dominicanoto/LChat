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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private ScrollPane chatScrollPane;

    @FXML
    private VBox messagesBox;

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

    private final Map<String, List<Label>>
            outgoingStatuses =
            new HashMap<>();

    private final ObservableList<User>
            dialogUsers =
            FXCollections.observableArrayList();

    private String lastRenderedDate;

    @FXML
    public void initialize() {

        ThemeService.applyTheme(
                rootPane,
                themeButton
        );

        loadCurrentUser();

        chatScrollPane.setVisible(false);

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
                            setGraphic(null);

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

                            Label onlineDot =
                                    new Label("●");

                            onlineDot.getStyleClass().add(
                                    online
                                            ? "dialog-online-dot"
                                            : "dialog-offline-dot"
                            );

                            Label name =
                                    new Label(user.getName());

                            name.getStyleClass().add(
                                    "dialog-name"
                            );

                            int dialogId =
                                    DialogService.findDialog(
                                            Session.getUsername(),
                                            user.getUsername()
                                    );

                            Label lastMessage =
                                    new Label(
                                            MessageService.getLastMessage(
                                                    dialogId
                                            )
                                    );

                            lastMessage.getStyleClass().add(
                                    "dialog-last-message"
                            );

                            VBox textBox =
                                    new VBox(
                                            3,
                                            name,
                                            lastMessage
                                    );

                            HBox cell =
                                    new HBox(
                                            8,
                                            onlineDot,
                                            textBox
                                    );

                            cell.setAlignment(Pos.CENTER_LEFT);

                            if (unread > 0) {

                                Label unreadLabel =
                                        new Label(
                                                String.valueOf(unread)
                                        );

                                unreadLabel.getStyleClass().add(
                                        "unread-badge"
                                );

                                Region spacer =
                                        new Region();

                                HBox.setHgrow(
                                        spacer,
                                        Priority.ALWAYS
                                );

                                cell.getChildren().addAll(
                                        spacer,
                                        unreadLabel
                                );
                            }

                            setText(null);
                            setGraphic(cell);
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

            if ("read".equals(
                    xmlMessage.type()
            )) {

                markMessagesRead(
                        xmlMessage.reader()
                );

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

                ensureDateSeparator(
                        LocalDate.now().toString()
                );

                addMessageBubble(
                        sender,
                        text,
                        false,
                        null,
                        currentTime()
                );

                SocketClient.sendRead(sender);

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
            OnlineService.clear();

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

        boolean hadUnreadMessages =
                unreadMessages.getOrDefault(
                        user.getUsername(),
                        0
                ) > 0 ||
                        MessageService.hasUnreadMessages(
                                user.getUsername(),
                                Session.getUsername()
                        );

        nameLabel.setText(
                user.getName()
        );

        usernameLabel.setText(
                "@" + user.getUsername()
        );

        chatScrollPane.setVisible(true);

        messageField.setVisible(true);

        sendButton.setVisible(true);

        currentDialogId =
                DialogService.findDialog(
                        Session.getUsername(),
                        user.getUsername()
                );

        loadMessages();

        unreadMessages.remove(
                user.getUsername()
        );

        updateDialogs();

        updateStatus();

        if (hadUnreadMessages) {

            SocketClient.sendRead(
                    user.getUsername()
            );
        }

        sendButton.setOnAction(
                event -> sendMessage()
        );
    }

    private Label addMessageBubble(
            String sender,
            String text,
            boolean outgoing,
            String status,
            String time
    ) {

        HBox row =
                new HBox();

        row.setAlignment(
                outgoing
                        ? Pos.CENTER_RIGHT
                        : Pos.CENTER_LEFT
        );

        Label name =
                new Label(
                        getDisplayName(sender)
                );

        name.getStyleClass().add("bubble-name");

        Label message =
                new Label(text);

        message.setWrapText(true);
        message.setMaxWidth(360);
        message.getStyleClass().add("bubble-text");

        VBox bubble =
                new VBox(4);

        bubble.getStyleClass().add(
                outgoing
                        ? "outgoing-bubble"
                        : "incoming-bubble"
        );

        bubble.getChildren().addAll(
                name,
                message
        );

        Label statusLabel = null;

        if (outgoing) {

            statusLabel =
                    new Label(
                            "read".equals(status)
                                    ? "✓✓"
                                    : "✓"
                    );

            statusLabel.getStyleClass().addAll(
                    "message-status",
                    statusClass(status)
            );

            Label timeLabel =
                    new Label(time);

            timeLabel.getStyleClass().add(
                    "message-time"
            );

            HBox statusRow =
                    new HBox(
                            5,
                            timeLabel,
                            statusLabel
                    );

            statusRow.setAlignment(Pos.CENTER_RIGHT);
            bubble.getChildren().add(statusRow);
        }

        row.getChildren().add(bubble);

        VBox.setMargin(
                row,
                new Insets(0, 8, 0, 8)
        );

        messagesBox.getChildren().add(row);
        chatScrollPane.setVvalue(1.0);

        return statusLabel;
    }

    private String currentTime() {

        return LocalTime.now()
                .format(
                        DateTimeFormatter
                                .ofPattern("HH:mm")
                );
    }

    private String getDisplayName(
            String username
    ) {

        User user =
                UserService.getUser(username);

        if (user == null) {
            return username;
        }

        return user.getName();
    }

    private String statusClass(
            String status
    ) {

        if ("read".equals(status)) {
            return "status-read";
        }

        if ("sent".equals(status)) {
            return "status-sent";
        }

        return "status-sending";
    }

    private void markMessagesRead(
            String reader
    ) {

        List<Label> labels =
                outgoingStatuses.get(reader);

        if (labels == null) {
            return;
        }

        for (Label label : labels) {

            label.setText("✓✓");

            label.getStyleClass().setAll(
                    "message-status",
                    "status-read"
            );
        }
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

        if (currentDialogId == -1) {

            currentDialogId =
                    DialogService.getOrCreateDialog(
                            sender,
                            receiver
                    );
        }

        ensureDateSeparator(
                LocalDate.now().toString()
        );

        Label statusLabel =
                addMessageBubble(
                        sender,
                        text,
                        true,
                        "sending",
                        currentTime()
                );

        boolean sent =
                SocketClient.sendMessage(
                        receiver,
                        text
                );

        if (!sent) {

            statusLabel.getStyleClass().setAll(
                    "message-status",
                    "status-sending"
            );

            return;
        }

        statusLabel.getStyleClass().setAll(
                "message-status",
                "status-sent"
        );

        outgoingStatuses
                .computeIfAbsent(
                        receiver,
                        key -> new ArrayList<>()
                )
                .add(statusLabel);

        messageField.clear();

        loadDialogs();
    }

    private void loadMessages() {

        messagesBox.getChildren().clear();
        lastRenderedDate = null;

        if (currentDialogId == -1) {
            return;
        }

        for (MessageService.MessageRecord message :
                MessageService.loadMessageRecords(
                        currentDialogId
                )) {

            String sender =
                    message.sender();

            String text =
                    message.message();

            boolean outgoing =
                    sender.equals(
                            Session.getUsername()
                    );

            ensureDateSeparator(
                    message.date()
            );

            Label statusLabel =
                    addMessageBubble(
                    sender,
                    text,
                    outgoing,
                    outgoing
                            ? message.read()
                                    ? "read"
                                    : "sent"
                            : null,
                    message.time()
            );

            if (outgoing && selectedUser != null) {

                outgoingStatuses
                        .computeIfAbsent(
                                selectedUser.getUsername(),
                                key -> new ArrayList<>()
                        )
                        .add(statusLabel);
            }
        }
    }

    private void ensureDateSeparator(
            String date
    ) {

        if (date == null ||
                date.isBlank() ||
                date.equals(lastRenderedDate)) {
            return;
        }

        lastRenderedDate = date;

        Label label =
                new Label(
                        formatDateSeparator(date)
                );

        label.getStyleClass().add(
                "date-separator"
        );

        HBox row =
                new HBox(label);

        row.setAlignment(Pos.CENTER);

        messagesBox.getChildren().add(row);
    }

    private String formatDateSeparator(
            String date
    ) {

        LocalDate messageDate =
                LocalDate.parse(date);

        if (messageDate.equals(LocalDate.now())) {
            return "Today";
        }

        return messageDate.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        );
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

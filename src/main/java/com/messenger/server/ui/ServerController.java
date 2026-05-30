package com.messenger.server.ui;

import com.messenger.server.Server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerController {

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea logArea;

    @FXML
    private ListView<String> conversationsList;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    private final ObservableList<String>
            conversations =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        conversationsList.setItems(conversations);

        Server.setEventListener(
                new Server.ServerEventListener() {

                    @Override
                    public void onLog(String message) {

                        appendLog(message);
                    }

                    @Override
                    public void onConversation(
                            String conversation
                    ) {

                        Platform.runLater(() -> {

                            if (!conversations.contains(
                                    conversation
                            )) {

                                conversations.add(
                                        conversation
                                );
                            }
                        });
                    }

                    @Override
                    public void onStatus(String status) {

                        Platform.runLater(() ->
                                statusLabel.setText(status)
                        );
                    }
                }
        );

        startButton.setOnAction(
                event -> Server.start()
        );

        stopButton.setOnAction(
                event -> Server.stop()
        );

        Server.start();
    }

    private void appendLog(
            String message
    ) {

        Platform.runLater(() -> {

            String time =
                    LocalTime.now()
                            .format(
                                    DateTimeFormatter
                                            .ofPattern("HH:mm:ss")
                            );

            logArea.appendText(
                    "[" + time + "] " +
                            message + "\n"
            );
        });
    }
}

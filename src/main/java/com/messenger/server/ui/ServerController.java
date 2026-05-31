package com.messenger.server.ui;

import com.messenger.protocol.XmlProtocol;
import com.messenger.server.Server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

    private static final int SERVER_PORT = 12345;

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
                event -> stopServer()
        );

        Server.start();
    }

    private void stopServer() {

        if (Server.isRunning()) {

            Server.stop();
            return;
        }

        try (
                Socket socket =
                        new Socket(
                                "localhost",
                                SERVER_PORT
                        );

                PrintWriter writer =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream(),
                                        StandardCharsets.UTF_8
                                ),
                                true
                        )
        ) {

            writer.println(
                    XmlProtocol.admin("stop")
            );

            statusLabel.setText("Stop requested");
            appendLog("Stop requested for external server");

        } catch (Exception e) {

            statusLabel.setText("Stopped");
            appendLog("No running server found");
        }
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

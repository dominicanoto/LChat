package com.messenger.client;

import com.messenger.protocol.XmlProtocol;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClient {

    private static Socket socket;

    private static BufferedReader reader;

    private static PrintWriter writer;

    public static void connect(
            String username
    ) {

        try {

            socket =
                    new Socket(
                            "localhost",
                            12345
                    );

            reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(),
                                    StandardCharsets.UTF_8
                            )
                    );

            writer =
                    new PrintWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(),
                                    StandardCharsets.UTF_8
                            ),
                            true
                    );

            writer.println(
                    XmlProtocol.login(username)
            );

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static void sendMessage(
            String receiver,
            String message
    ) {

        writer.println(
                XmlProtocol.chat(
                        Session.getUsername(),
                        receiver,
                        message
                )
        );
    }

    public static void listen(
            MessageListener listener
    ) {

        new Thread(() -> {

            try {

                String message;

                while ((message =
                        reader.readLine()) != null) {

                    String finalMessage =
                            message;

                    Platform.runLater(() ->
                            listener.onMessage(
                                    finalMessage
                            )
                    );
                }

            } catch (IOException e) {

                disconnect();

            }

        }).start();
    }

    public static void disconnect() {

        try {

            if (socket != null) {

                socket.close();
            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}

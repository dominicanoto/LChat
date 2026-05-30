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

    public static boolean connect(
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

            return true;

        } catch (IOException e) {

            e.printStackTrace();
            disconnect();

            return false;
        }
    }

    public static boolean sendMessage(
            String receiver,
            String message
    ) {

        if (writer == null) {
            return false;
        }

        writer.println(
                XmlProtocol.chat(
                        Session.getUsername(),
                        receiver,
                        message
                )
        );

        return true;
    }

    public static void sendRead(
            String sender
    ) {

        if (writer == null) {
            return;
        }

        writer.println(
                XmlProtocol.read(
                        Session.getUsername(),
                        sender
                )
        );
    }

    public static void listen(
            MessageListener listener
    ) {

        if (reader == null) {
            return;
        }

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

            if (writer != null &&
                    Session.getUsername() != null) {

                writer.println(
                        XmlProtocol.logout(
                                Session.getUsername()
                        )
                );

                writer.flush();
            }

            if (socket != null) {

                socket.close();
            }

            socket = null;
            reader = null;
            writer = null;

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}

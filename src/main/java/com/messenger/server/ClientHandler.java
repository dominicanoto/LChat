package com.messenger.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private Socket socket;

    private BufferedReader reader;

    private PrintWriter writer;

    private String username;

    public ClientHandler(Socket socket) {

        this.socket = socket;

        try {

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            writer = new PrintWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream(),
                            StandardCharsets.UTF_8
                    ),
                    true
            );

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @Override
    public void run() {

        try {

            // First message = username

            username = reader.readLine();

            Server.addClient(
                    username,
                    this
            );

            String message;

            while ((message = reader.readLine()) != null) {

                // FORMAT:
                // receiver:message

                int separator =
                        message.indexOf(":");

                if (separator == -1) {
                    continue;
                }

                String receiver =
                        message.substring(
                                0,
                                separator
                        );

                String text =
                        message.substring(
                                separator + 1
                        );

                String formatted =
                        username + ": " + text;

                Server.sendToUser(
                        receiver,
                        formatted
                );
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            Server.removeClient(username);

        }
    }

    public void sendMessage(
            String message
    ) {

        writer.println(message);

    }
}
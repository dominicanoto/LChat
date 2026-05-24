package com.messenger.client;

import java.io.*;
import java.net.Socket;

public class SocketClient {

    private Socket socket;

    private PrintWriter writer;

    private BufferedReader reader;

    public void connect(String host) {

        try {

            socket = new Socket(host, 1234);

            writer = new PrintWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream(),
                            java.nio.charset.StandardCharsets.UTF_8
                    ),
                    true
            );

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            java.nio.charset.StandardCharsets.UTF_8
                    )
            );

            System.out.println("Connected!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {

        writer.println(message);

    }

    public void listenForMessages(MessageListener listener) {

        new Thread(() -> {

            try {

                String message;

                while ((message = reader.readLine()) != null) {

                    listener.onMessage(message);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
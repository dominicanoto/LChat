package com.messenger.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;

    private BufferedReader reader;

    private PrintWriter writer;

    public ClientHandler(Socket socket) {

        this.socket = socket;

        try {

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            java.nio.charset.StandardCharsets.UTF_8
                    )
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {

        writer.println(message);

    }

    @Override
    public void run() {

        try {

            String message;

            while ((message = reader.readLine()) != null) {

                System.out.println(message);

                Server.broadcast(message);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
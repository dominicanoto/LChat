package com.messenger.server;

import com.messenger.protocol.XmlProtocol;
import com.messenger.protocol.XmlProtocol.XmlMessage;

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

            // First message = XML login

            XmlMessage loginMessage =
                    XmlProtocol.parse(
                            reader.readLine()
                    );

            username =
                    loginMessage.username();

            Server.addClient(
                    username,
                    this
            );

            String message;

            while ((message = reader.readLine()) != null) {

                XmlMessage xmlMessage =
                        XmlProtocol.parse(message);

                if ("logout".equals(
                        xmlMessage.type()
                )) {
                    break;
                }

                if ("read".equals(
                        xmlMessage.type()
                )) {

                    Server.sendReadReceipt(
                            xmlMessage.sender(),
                            xmlMessage.reader()
                    );

                    continue;
                }

                if (!"chat".equals(
                        xmlMessage.type()
                )) {
                    continue;
                }

                String receiver =
                        xmlMessage.receiver();

                String text =
                        xmlMessage.text();

                String formatted =
                        XmlProtocol.chat(
                                username,
                                receiver,
                                text
                        );

                Server.registerConversation(
                        username,
                        receiver,
                        text
                );

                Server.sendToUser(
                        receiver,
                        formatted
                );
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            Server.removeClient(
                    username,
                    this
            );

        }
    }

    public void sendMessage(
            String message
    ) {

        writer.println(message);

    }

    public void close() {

        try {

            socket.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}

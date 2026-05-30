package com.messenger.protocol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlProtocol {

    public static String login(String username) {

        return createMessage(
                "login",
                new String[][]{
                        {"username", username}
                }
        );
    }

    public static String chat(
            String sender,
            String receiver,
            String text
    ) {

        return createMessage(
                "chat",
                new String[][]{
                        {"sender", sender},
                        {"receiver", receiver},
                        {"text", text}
                }
        );
    }

    public static String logout(
            String username
    ) {

        return createMessage(
                "logout",
                new String[][]{
                        {"username", username}
                }
        );
    }

    public static String read(
            String reader,
            String sender
    ) {

        return createMessage(
                "read",
                new String[][]{
                        {"reader", reader},
                        {"sender", sender}
                }
        );
    }

    public static String presence(
            String username,
            String status
    ) {

        return createMessage(
                "presence",
                new String[][]{
                        {"username", username},
                        {"status", status}
                }
        );
    }

    public static XmlMessage parse(String xml) {

        try {

            Document document =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(
                                    new InputSource(
                                            new StringReader(xml)
                                    )
                            );

            Element root =
                    document.getDocumentElement();

            return new XmlMessage(
                    root.getAttribute("type"),
                    getText(root, "username"),
                    getText(root, "sender"),
                    getText(root, "receiver"),
                    getText(root, "text"),
                    getText(root, "status"),
                    getText(root, "reader")
            );

        } catch (Exception e) {

            return XmlMessage.invalid();
        }
    }

    private static String createMessage(
            String type,
            String[][] fields
    ) {

        try {

            Document document =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .newDocument();

            Element root =
                    document.createElement("message");

            root.setAttribute("type", type);
            document.appendChild(root);

            for (String[] field : fields) {

                Element element =
                        document.createElement(field[0]);

                element.setTextContent(field[1]);
                root.appendChild(element);
            }

            StringWriter writer =
                    new StringWriter();

            TransformerFactory
                    .newInstance()
                    .newTransformer()
                    .transform(
                            new DOMSource(document),
                            new StreamResult(writer)
                    );

            return writer
                    .toString()
                    .replace(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
                            ""
                    )
                    .trim();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot create XML message",
                    e
            );
        }
    }

    private static String getText(
            Element root,
            String tag
    ) {

        if (root.getElementsByTagName(tag).getLength() == 0) {
            return "";
        }

        return root
                .getElementsByTagName(tag)
                .item(0)
                .getTextContent();
    }

    public record XmlMessage(
            String type,
            String username,
            String sender,
            String receiver,
            String text,
            String status,
            String reader
    ) {

        public static XmlMessage invalid() {

            return new XmlMessage(
                    "invalid",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );
        }
    }
}

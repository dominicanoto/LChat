# LChat

LChat is a desktop messenger application built with Java, JavaFX, SQLite and a custom XML-based client-server protocol.

The application allows users to register, log in, exchange messages with other users, see online/offline status, receive read receipts and use a graphical server interface.

## Features

- User registration and login
- Client-server message exchange
- SQLite database for users, dialogs and messages
- Custom XML protocol for communication
- Online and offline user status
- Message read receipts
- Chat interface with message bubbles
- Light and dark themes
- Display name editing
- Logout support
- Graphical server application

## Technologies

- Java
- JavaFX
- SQLite
- Maven
- XML
- CSS

## Project Structure

```text
src/main/java/com/messenger
├── client        # Client connection, session and theme logic
├── controller    # JavaFX controllers
├── database      # SQLite database services
├── model         # Data models
├── protocol      # XML protocol
├── server        # Server and client handler logic
└── Main.java     # Client application entry point

src/main/resources
├── css           # Application styles
└── fxml          # JavaFX layouts

package org.example;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Logger;

public class Connection extends Thread {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final XmlMapper mapper = new XmlMapper();
    private final Chat chat;
    private final UUID sessionId;
    private final Session session;

    public Connection(Chat chat, Socket socket) throws IOException {
        this.chat = chat;
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            Command command = readCommand(in);
            if (!command.getCommandName().equals("login") || command.getUsername().isEmpty() || command.getPassword().isEmpty()) {
                throw new RuntimeException("Invalid command=%s".formatted(command.toString()));
            }
            this.sessionId = chat.register(new User(command.getUsername(), command.getPassword()), socket);
            this.session = chat.getSession(sessionId);
        }
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(session.socket().getInputStream())) {
            while (session.socket().isConnected()) {
                if (session.isTimeout(chat.getTimeOutInMinutes())) {
                    chat.logout(sessionId);
                    return;
                }
            }
            Command command = readCommand(in);
            switch (command.getCommandName()) {
                case "list" -> chat.sendRegisteredUsers(sessionId);
                case "message" -> chat.sendMessage(sessionId, command.getMessage());
                case "logout" -> chat.logout(sessionId);
                default -> chat.sendErrorMessage(sessionId, "Unknown command");
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }

    private Command readCommand(DataInputStream in) throws IOException {
        StringJoiner joiner = new StringJoiner("");
        try {
            int messageLength = in.readInt();
            while (messageLength > joiner.length()) {
                joiner.add(in.readUTF());
            }
            return mapper.readValue(joiner.toString(), Command.class);
        } finally {
            chat.logout(sessionId);
        }
    }
}

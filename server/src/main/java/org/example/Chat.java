package org.example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static org.example.Response.*;

public class Chat {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final LinkedList<String> messages = new LinkedList<>();
    private final Lock lock = new ReentrantLock();
    private final String chatName;
    private final Long messageCapacity;
    private final Long timeOutInMinutes;

    public Chat() {
        this.chatName = "Simple Chat";
        this.messageCapacity = 100L;
        this.timeOutInMinutes = 5L;
    }

    public Chat(String chatName, Long messageCapacity, Long timeOutInMinutes) {
        this.chatName = chatName;
        this.messageCapacity = messageCapacity;
        this.timeOutInMinutes = timeOutInMinutes;
    }

    public Session getSession(UUID sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new RuntimeException("No session found for sessionId=%s".formatted(sessionId));
        }
        return sessions.get(sessionId);
    }

    public Long getTimeOutInMinutes() {
        return timeOutInMinutes;
    }

    public void sendMessage(UUID sessionId, String message) {
        if (!sessions.containsKey(sessionId) || !sessions.get(sessionId).socket().isConnected()) {
            return;
        }
        lock.lock();
        if (messages.size() >= messageCapacity) {
            messages.removeFirst();
        }
        messages.addLast(message);
        String response = EVENT_RESPONSE.formatted(
                "message",
                FROM_RESPONSE.formatted(chatName) + MESSAGE_RESPONSE.formatted(message)
        );
        sendResponseToOnlineUsers(response);
        sessions.get(sessionId).extend();
        lock.unlock();
    }

    public UUID register(User user, Socket socket) {
        if (!users.containsKey(user.username())) {
            users.put(user.username(), user);
            return login(new Session(user.username(), socket));
        }
        if (!users.get(user.username()).password().equals(user.password())) {
            String response = ERROR_RESPONSE.formatted("Invalid username or password");
            sendResponse(socket, response);
            try {
                socket.close();
            } catch (IOException e) {
                logger.warning("Failed to close socket. " + e.getMessage());
            }
            throw new RuntimeException("Invalid username or password");
        }
        return login(new Session(user.username(), socket));
    }

    public void logout(UUID sessionId) {
        if (!sessions.containsKey(sessionId)) {
            return;
        }
        lock.lock();
        try {
            Session session = sessions.get(sessionId);
            String response = EVENT_RESPONSE.formatted("userlogout", NAME_RESPONSE.formatted(session.username()));
            sendResponseToOnlineUsers(response);
            if (session.socket().isConnected()) {
                session.socket().close();
            }
            sessions.remove(sessionId);
        } catch (IOException e) {
            logger.warning("Failed to close session. " + e.getMessage());
        }
        lock.unlock();
    }

    public void sendRegisteredUsers(UUID sessionId) {
        if (!sessions.containsKey(sessionId) || !sessions.get(sessionId).socket().isConnected()) {
            return;
        }
        String response = SUCCESS_RESPONSE.formatted(
                String.join(
                        "",
                        users.values().stream()
                                .map(user -> USER_RESPONSE.formatted(user.username()))
                                .toList()
                )
        );
        Session session = sessions.get(sessionId);
        sendResponse(session.socket(), response);
        session.extend();
    }

    public void sendErrorMessage(UUID sessionId, String message) {
        if (!sessions.containsKey(sessionId) || !sessions.get(sessionId).socket().isConnected()) {
            return;
        }
        lock.lock();
        String response = ERROR_RESPONSE.formatted(MESSAGE_RESPONSE.formatted(message));
        sendResponse(sessions.get(sessionId).socket(), response);
        lock.unlock();
    }

    private UUID login(Session session) {
        lock.lock();
        UUID uuid = UUID.randomUUID();
        sessions.put(uuid, session);
        String response = EVENT_RESPONSE.formatted("userlogin", NAME_RESPONSE.formatted(session.username()));
        sendResponseToOnlineUsers(response);
        lock.unlock();

        return uuid;
    }

    private void sendResponseToOnlineUsers(String response) {
        sessions.values().stream()
                .map(Session::socket)
                .filter(Socket::isConnected)
                .forEach(socket -> sendResponse(socket, response));
    }

    private void sendResponse(Socket socket, String response) {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            out.writeUTF(response);
        } catch (IOException e) {
            logger.warning("Failed to send response. " + e.getMessage());
        }
    }
}

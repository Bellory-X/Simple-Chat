package org.example;

import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class Session {

    private ZonedDateTime lastActionTime = ZonedDateTime.now();
    private final String username;
    private final Socket socket;

    public Session(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
    }

    public String username() {
        return username;
    }

    public Socket socket() {
        return socket;
    }

    public boolean isTimeout(Long timeoutInMinutes) {
        return lastActionTime.plusMinutes(timeoutInMinutes).isBefore(ZonedDateTime.now());
    }

    public void extend() {
        lastActionTime = ZonedDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Session) obj;
        return Objects.equals(this.username, that.username) &&
                Objects.equals(this.socket, that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, socket);
    }

    @Override
    public String toString() {
        return "Session[" +
                "username=" + username + ", " +
                "socket=" + socket + ']';
    }
}

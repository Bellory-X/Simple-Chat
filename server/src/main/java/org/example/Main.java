package org.example;

public class Main {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, 10);
        server.run();
    }
}
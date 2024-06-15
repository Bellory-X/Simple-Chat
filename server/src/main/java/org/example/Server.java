package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {

    private final Logger logger = Logger.getLogger(Server.class.getName());
    private final String host;
    private final Integer port;
    private final Integer connectionCount;
    private final ExecutorService executorService;
    private final Chat chat;

    public Server(String host, Integer port, Integer connectionCount) {
        this.host = host;
        this.port = port;
        this.connectionCount = connectionCount;
        executorService = Executors.newFixedThreadPool(connectionCount);
        chat = new Chat();
    }

    public Server(
            String host,
            Integer port,
            Integer connectionCount,
            String chatName,
            Long messageCapacity,
            Long timeOutInMinutes
    ) {
        this.host = host;
        this.port = port;
        this.connectionCount = connectionCount;
        executorService = Executors.newFixedThreadPool(connectionCount);
        chat = new Chat(chatName, messageCapacity, timeOutInMinutes);
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port, connectionCount, InetAddress.getByName(host))) {
            Thread stopThread = createStopThread();
            stopThread.start();
            while (stopThread.isAlive()) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(chat, socket);
                    executorService.submit(connection);
                } catch (IOException | RuntimeException e) {
                    logger.warning(e.getMessage());
                }
            }
            executorService.shutdown();
            executorService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Thread createStopThread() {
        return new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.next().equals("stop")) {
                    return;
                }
            }
        });
    }
}

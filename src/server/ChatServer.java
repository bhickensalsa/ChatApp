package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.chatapp.cryptography.*;

/**
 * ChatServer listens for incoming client connections, receives encrypted messages,
 * decrypts them, and broadcasts them to all connected clients after re-encrypting.
 * The ChatServer relies on the class ClientHandler that handles each client and
 * the message sending/recieving. 
 * 
 * It uses one thread per client connection.
 * 
 * @author Leonard Smedberg
 * @version 2025-04-15
 */

public class ChatServer {
    private final int port;
    private final MessageEncryptor encryptor;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>(); // Thread-safe list type

    public ChatServer(int port, MessageEncryptor encryptor) {
        this.port = port;
        this.encryptor = encryptor;
    }


    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for clients...");

            while(true) {

                // Accept each new client, and make a new instance of ClientHandler for each for threading
                Socket clientPort = serverSocket.accept();
                System.out.println("Accepted client: " + clientPort.getInetAddress());

                ClientHandler handler = new ClientHandler(this, encryptor, clientPort);
                clients.add(handler);

                // Launch client thread
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Broadcast the encrypted message to all clients connected to ChatServer. 
     */
    public void broadcast(String encryptedMsg, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // Prevent echoing back to the sender
                client.sendMessage(encryptedMsg);
            }
        }
    }

    public ClientHandler removeClient(ClientHandler client) {
        clients.remove(client);
        return client;
    }

    public static void main(String[] args) {
        MessageEncryptor encryptor = new DummyEncryptor(); // replace with real encryptor class
        ChatServer server = new ChatServer(12345, encryptor); // Listens on port 12345
        server.start();
    }
}

// TODO change encryptor variable definition in main to use the real encryptor class
// TODO Add a specific and global port other than 12345 (in main)
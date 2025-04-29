package com.chatapp.server;

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
 * The `ChatServer` handles incoming messages from clients and sends them to the server to be broadcasted. 
 * To achieve this with proper encryption, the server/client interaction goes like this: 
 * > The client first encrypts the message it is trying to send using the `server's public key`, which it 
 * > recieves as the `ClientHandler`'s first message to the client. As the server recieves that encrypted message,
 * > `ClientHandler` decrypts it and broadcasts the decrypted plaintext message to the server. The `ChatServer`
 * > then _reencrypts it for each connected client_ using their saved `clientPublicKey` in `ClientHandler`, 
 * > which is done inside the broadcast() method.
 * 
 * @author Leonard Smedberg
 * @version 2025-04-27
 */

public class ChatServer {
    private final int port;
    private final MessageEncryptor encryptor;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>(); // Thread-safe list type

    public ChatServer(int port, MessageEncryptor encryptor) {
        this.port = port;
        this.encryptor = encryptor;
    }

    /**
     * Start the server.
     * 
     * When started, server runs indefinitely, always listening on `port` socket for
     * accepting clients seeking to connect on said socket. For each client that connects,
     * a new instance of `ClientHandler` is launched through which the server can interact
     * with the client. 
     * 
     * The server keeps a global thread-safe list, `clients`, to keep track of all connected
     * clients.
     */
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
     * 
     * Receives a plaintext string from ClientHandler -- i.e a message sent by a client to the server --
     * and broadcast() reencrypts the message separately for each client that will recieve it, using their
     * stored `clientPublicKey` in `ClientHandler`.
     */
    public void broadcast(String plainTextString, ClientHandler sender) throws Exception {
        for (ClientHandler client : clients) {
            if (client != sender) { // Prevent echoing back to the sender

                // Throw exception for individual fails to avoid crashing server for single send/encryption fails
                try {
                    client.sendMessage(encryptor.encrypt(plainTextString, client.getPublicKey()));
                } catch (IOException e) {
                    System.err.println("Failed to send to client: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Disconnect client.
     * 
     * @param client client to be disconnected.
     * @return       client that was removed.
     */
    public ClientHandler removeClient(ClientHandler client) {
        clients.remove(client);
        return client;
    }

    public static void main(String[] args) throws Exception {
        MessageEncryptor encryptor = new SecureMessenger(); 
        ChatServer server = new ChatServer(12345, encryptor); // Listens on port 12345
        server.start();
    }
}

// TODO Add a specific and global port other than 12345 (in main)
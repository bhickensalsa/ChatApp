package com.chatapp.server;

import java.io.*;
import java.net.*;

import com.chatapp.cryptography.*;

/**
 * ClassHandler handles interaction with each client connected to ChatServer. It handles each
 * client separately, using threading to remain responsive to all. 
 * 
 * @author Leonard Smedberg
 * @version 2025-04-15
 */
class ClientHandler extends Thread {
    // Most important definitions for ClientHandler
    private final Socket socket;    // Defined in imported server environment library
    private BufferedReader in;      // Reader for incoming messages
    private PrintWriter out;        // Writer for outgoing messages

    private final MessageEncryptor encryptor;
    private final ChatServer server;

    public ClientHandler(ChatServer server, MessageEncryptor encryptor, Socket socket) {
        this.server = server;
        this.encryptor = encryptor;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String encryptedMessage;
            while ((encryptedMessage = in.readLine()) != null) {
                try {
                    String decrypted = encryptor.decrypt(encryptedMessage);
                    System.out.println("Received: " + decrypted);

                    // Re-encrypt before broadcasting
                    String reEncrypted = encryptor.encrypt(decrypted);
                    server.broadcast(reEncrypted, this); // Call server as broadcast should send to all other clients on server

                } catch (Exception e) {
                    System.err.println("Decryption failed: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } 
        
        // Always try to close socket and remove client when they disconnect
        finally {
            try {
                socket.close();
                server.removeClient(this);
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String encryptedMessage) {
        out.println(encryptedMessage);
    }

}
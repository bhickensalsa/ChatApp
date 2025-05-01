package com.chatapp;

import com.chatapp.client.*;
import com.chatapp.cryptography.*;
import com.chatapp.gui.*;
import com.chatapp.server.*;

/**
 * Early draft launcher class for starting the chat app
 * Starts a server instance, waits one second, then starts a client with GUI.
 * 
 * @version 2025-04-30
 */
public class launcher {
    public static void main(String[] args) throws Exception {
        int port = 12345;
        String host = "localhost";
        
        new Thread(() -> {
            try {
                MessageEncryptor serverEncryptor = new SecureMessenger();
                ChatServer server = new ChatServer(port, serverEncryptor);
                server.start();
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
            }
        }).start();

        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            SecureMessenger clientEncryptor = new SecureMessenger();
            Client client = new Client(host, port, clientEncryptor);
            client.start();
            new ChatGUI(client);
        } catch (Exception e) {
            System.err.println("Failed to start client: " + e.getMessage());
        }
    }
}
package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.chatapp.cryptography.*;

/**
 * ClientHandler handles interaction with each client connected to ChatServer. It handles each
 * client separately, using threading to remain responsive to all. 
 * 
 * ClientHandler also handles the public key exchange with the clients, and partly sends the server's public 
 * key to the client, partly recieves the client's key. Encryption on the client's behalf is made using the
 * server key, so only the server can read it. 
 * 
 * @author Leonard Smedberg
 * @version 2025-04-27
 */
class ClientHandler extends Thread {
    private final Socket socket;    
    private BufferedReader in;      // Reader for client socket input stream
    private PrintWriter out;        // Writer for client socket output stream

    private final MessageEncryptor encryptor;
    private final ChatServer server;

    private PublicKey clientPublicKey = null; // Init as null

    public ClientHandler(ChatServer server, MessageEncryptor encryptor, Socket socket) {
        this.server = server;
        this.encryptor = encryptor;
        this.socket = socket;
    }

    public PublicKey getPublicKey() {
        return clientPublicKey;
    }

    /**
     * Runs the thread for a single client. This is run when the server runs `<Client>.start()`.
     * 
     * Reads and writes to clients. First message it reads is the public key from the client;
     * the first message it writes is the server's public key to the client. 
     * 
     * As the client first sends its own public key, and then awaits that of the server, the server
     * instead first receives the client's key and then sends its own to the client, which makes
     * the process fan out properly in the end.
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Define client socket input stream as a reader
            out = new PrintWriter(socket.getOutputStream(), true);                    // Define client socket output stream as a writer

            /**
             * Client/server encryption key exchange
             */

            // Read the client's public encryption key
            String clientKeyBase64 = in.readLine(); // Read from client socket input stream
            byte[] keyBytes = Base64.getDecoder().decode(clientKeyBase64);

            // Turn fetched key bytes into a usable public key
            try {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

                // Save client key for re-encryption purposes
                clientPublicKey = factory.generatePublic(spec); 

            } catch (GeneralSecurityException e) {
                System.err.println("Key construction failed: " + e.getMessage());
            }

            System.out.println("Server: Client public key received.");
            System.out.println("Sending server public key...");

            // Forge and broadcast server public key to client socket output stream
            String serverKeyBase64 = Base64.getEncoder().encodeToString(encryptor.getPublicKey().getEncoded());
            out.println(serverKeyBase64);

            // Cancel process and close connection upon any error in key generation
            if (clientPublicKey == null) {
                return;
            }

            /**
             * Handle incoming messages from clients
             */

            String encryptedMessage;

            while ((encryptedMessage = in.readLine()) != null) {
                try {
                    String plainText = encryptor.decrypt(encryptedMessage);
                    System.out.println("Received: " + plainText);

                    // Broadcast the by the client sent message to the server as **plaintext**
                    // Re-encryption takes place in the ChatServer class
                    server.broadcast(plainText, this);

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

    /**
     * Send message to client.
     * 
     * Command is enclosed in precautions about the socket. If somehow `out` fails to be
     * initialized, or errors occur in the socket due to heavy threading, SendMessage would
     * crash. To avoid this, only send message if `out` is not null and the socket is open.
     * 
     * @param message  Message to send
     */
    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

}
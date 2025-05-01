package com.chatapp.client;

import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.function.Consumer;

import com.chatapp.cryptography.*;

/**
 * A secure chat client that connects to a server over sockets, performs RSA key exchange,
 * and enables encrypted communication using a {@link MessageEncryptor}.
 *
 * <p>Handles connection setup, key exchange, encrypted message sending, and receiving.
 * Messages are read from the console and sent to the server; incoming messages are
 * decrypted and displayed.
 *
 * Updated to work alongside the ChatGUI.java, instead of the 
 * previous terminal chat configuration.
 * 
 * This class requires an encrypter using the `MessageEncryptor` interface
 * 
 * @author Philip Jonsson
 * @version 2025-04-30
 */
public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String host;
    private final int port;
    private final MessageEncryptor encryptor;
    private PublicKey serverPublicKey;
    private boolean running;
    private Consumer<String> consumer;

    /**
     * Client constructor
     */
    public Client(String host, int port, MessageEncryptor encryptor) throws Exception {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.host = host;
        this.port = port;
        this.encryptor = encryptor;
        this.running = true;
    }

    /**
     * Setter for the Consumer, which relays messages to the GUI.
     */
    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    /**
     * Entry point for the client. 
     * Connects to the server, sends/receives keys, and sets up I/O communication streams.
     */
    public void start() {
        try {
            log("Connected");

            // Send client's public key to server
            String clientKeyBase64 = Base64.getEncoder().encodeToString(encryptor.getPublicKey().getEncoded());
            out.println(clientKeyBase64);

            // Receive server's public key
            String serverKeyBase64 = in.readLine();
            serverPublicKey = convertBase64ToKey(serverKeyBase64);
            
            log("Key exchange complete.\n" + "Type your message and hit Enter. Type 'exit' to disconnect.");

            // Launch a thread to listen for incoming messages
            Thread listenerThread = new Thread(() -> listenForMessages(in));
            listenerThread.start();

            System.out.println("Disconnected.");

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    /**
     * Stops the client by closing the socket and streams.
     */
    public void stop() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }

    
    /**
     * Continuously listens for incoming messages from the server,
     * decrypts, and displays them.
     */
    private void listenForMessages(BufferedReader in) {
        try {
            String received;
            while ((received = in.readLine()) != null) {
                try {
                    String decrypted = encryptor.decrypt(received);
                    log("[Received] " + decrypted);
                } catch (Exception e) {
                    log("Decryption failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("Disconnected from server.");
        }
    }

    /**
     * Encrypts and sends a message to the server.
     * If "exit" is typed, the client stops.
     */
    public void sendMessage(String message) {
        if (running) {
            if ("exit".equalsIgnoreCase(message)) {
                log("Exiting chat...");
                stop();
                return;
            }
            if (serverPublicKey == null) {
                log("Cannot send messages: server's public key is missing.");
                return;
            }
            
            try {
                String encrypted = encryptor.encrypt(message, serverPublicKey);
                out.println(encrypted);
            } catch (Exception e) {
                System.err.println("Encryption failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Logs a message to both the terminal and the consumer, if one has been set.
     */
    public void log(String message) {
        System.err.println(message); // Log to terminal
        if (consumer != null) {
            consumer.accept(message);
        }
    }
    
    /**
     * Helper method to convert Base64 key to PublicKey
     */
    private PublicKey convertBase64ToKey(String serverKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(serverKeyBase64);
        // Convert byte array into PublicKey
        PublicKey serverPublicKey = null;
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            serverPublicKey = factory.generatePublic(spec);
        } catch (Exception e) {
            System.err.println("Key construction failed: " + e.getMessage());
        }
        return serverPublicKey;
    }

    /**
     * Main method: sets up an encryptor and starts the client.
     */
    public static void main(String[] args) {
        try {
            SecureMessenger encryptor = new SecureMessenger();
            Client client = new Client("localhost", 12345, encryptor);
            client.start();
        } catch (Exception e) {
            System.err.println("Failed to intialize SecureMessenger: " + e.getMessage());
        }
    }
}
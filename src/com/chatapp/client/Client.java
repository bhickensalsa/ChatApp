package com.chatapp.client;

import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import com.chatapp.cryptography.*;

/**
 * A secure chat client that connects to a server over sockets, performs RSA key exchange,
 * and enables encrypted communication using a {@link MessageEncryptor}.
 *
 * <p>Handles connection setup, key exchange, encrypted message sending, and receiving.
 * Messages are read from the console and sent to the server; incoming messages are
 * decrypted and displayed.
 *
 * <p>Use {@code start()} to initiate the connection and begin messaging.
 * 
 * This class requires an encrypter using the `MessageEncryptor` interface
 * 
 * @author Philip Jonsson
 * @version 2025-04-25
 */
public class Client {
    private final String host;
    private final int port;
    private final MessageEncryptor encryptor;

    public Client(String host, int port, MessageEncryptor encryptor) {
        this.host = host;
        this.port = port;
        this.encryptor = encryptor;
    }

    /*
     * Entry point for the client. 
     * Connects to the server, sends and receives keys, and sets up I/O streams for communication.
     */
    public void start() {
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Reader for incoming messages
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Writer for outgoing messages
            Scanner scanner = new Scanner(System.in) // Scanner for user input
            ) {

            System.out.println("Connected to the chat server.");
            System.out.println("Exchanging keys...");

            // Send client's public key to server
            String clientKeyBase64 = Base64.getEncoder().encodeToString(encryptor.getPublicKey().getEncoded());
            out.println(clientKeyBase64);

            // Receive server's public key
            String serverKeyBase64 = in.readLine();
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
            
            System.out.println("Key exchange complete.");
            System.out.println("Type your message and hit Enter. Type 'exit' to disconnect.");

            // Launch a separate thread to listen for incoming messages
            Thread listenerThread = new Thread(() -> listenForMessages(in));
            listenerThread.start();

            // Main thread handles sending user messages
            sendMessages(scanner, out, serverPublicKey);

            System.out.println("Disconnected.");

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    /*
     * Continuously listens for incoming messages from the server.
     * Decrypts and prints each received message.
     */
    private void listenForMessages(BufferedReader in) {
        try {
            String received;
            while ((received = in.readLine()) != null) {
                try {
                    String decrypted = encryptor.decrypt(received);
                    System.out.println("[Received] " + decrypted);
                } catch (Exception e) {
                    System.err.println("Decryption failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Disconnected from server.");
        }
    }

    /*
     * Reads user input, encrypts it, and sends it to the server.
     * Typing 'exit' will terminate the connection.
     */
    private void sendMessages(Scanner scanner, PrintWriter out, PublicKey serverPublicKey) {
        while (true) {
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                System.out.println("Exiting chat...");
                break;
            }
            if (serverPublicKey == null) {
                System.err.println("Cannot send messages: server's public key is missing.");
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

    /*
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
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client class for connecting to a chat server, sending encrypted messages, 
 * and receiving and decrypting messages from the server.
 * 
 * This class establishes a connection with the server using a specified host and port,
 * allows the user to input messages which are encrypted before being sent to the server,
 * and listens for incoming encrypted messages from the server, which are then decrypted
 * and displayed to the user. The user can exit the chat by typing 'exit'.
 * 
 * The class uses a separate thread to handle receiving messages while the main thread
 * handles sending messages to ensure smooth communication.
 * 
 * @author Philip Jonsson
 * @version 2025-04-14
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
     * Connects to the server and sets up I/O streams for communication.
     */
    public void start() {
        try (Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Reader for incoming messages
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Writer for outgoing messages
            Scanner scanner = new Scanner(System.in)) { // Scanner for user input

            System.out.println("Connected to the chat server.");
            System.out.println("Type your message and hit Enter. Type 'exit' to disconnect.");

            // Launch a separate thread to listen for incoming messages
            Thread listenerThread = new Thread(() -> listenForMessages(in));
            listenerThread.start();

            // Main thread handles sending user messages
            handleOutgoingMessages(scanner, out);

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
    private void handleOutgoingMessages(Scanner scanner, PrintWriter out) {
        while (true) {
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                System.out.println("Exiting chat...");
                break;
            }

            try {
                String encrypted = encryptor.encrypt(message);
                out.println(encrypted);
            } catch (Exception e) {
                System.err.println("Encryption failed: " + e.getMessage());
            }
        }
    }

    /*
     * Main method: sets up a dummy encryptor and starts the client.
     */
    public static void main(String[] args) {
        MessageEncryptor encryptor = new DummyEncryptor(); // Replace with real implementation when available
        Client client = new Client("localhost", 12345, encryptor);
        client.start();
    }
}
package com.chatapp.gui;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.chatapp.client.*;
import com.chatapp.cryptography.SecureMessenger;

/**
 * Early draft GUI
 * @author Philip Jonsosn
 * @version 2025-04-29
 */

public class ChatGUI {
    private Client client;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatGUI(Client client) {
        this.client = client;
        frame = new JFrame("ChatApp");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        
        inputField = new JTextField();
        sendButton = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> handleSend());
        inputField.addActionListener(e -> handleSend());

        frame.setVisible(true);
    }
    
    private void handleSend() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        if ("exit".equalsIgnoreCase(message)) {
            System.err.println("Exiting chat...");
            frame.dispose();
            client.stop();
        }
        try {
            client.sendMessage(message);
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
        } catch (Exception e) {
            chatArea.append("[Error] " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) throws Exception {
        SecureMessenger clientEncryptor = new SecureMessenger();
        Client client = new Client("localhost", 12345, clientEncryptor);
        client.start();
        ChatGUI gui = new ChatGUI(client);
    }
}

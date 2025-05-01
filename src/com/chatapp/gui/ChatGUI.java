package com.chatapp.gui;

import java.awt.*;
import javax.swing.*;

import com.chatapp.client.*;

/**
 * Early draft GUI
 * @author Philip Jonsson
 * @version 2025-04-30
 */
public class ChatGUI {
    private Client client;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    /**
     * Initialize GUI with components, and 
     * set Consumer to receive messages from client.
     * @param client
     */
    public ChatGUI(Client client) {
        this.client = client;

        frame = new JFrame("DD1349 - ChatApp"); // App window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // === Chat Area ===
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        frame.add(chatScrollPane, BorderLayout.CENTER);

        // === Input Panel ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // === User List Panel ===
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBorder(BorderFactory.createTitledBorder("Users"));
        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setPreferredSize(new Dimension(200, 0)); // width only
        frame.add(userListScrollPane, BorderLayout.EAST);

        // === Event Listeners ===
        sendButton.addActionListener(e -> handleSend());
        inputField.addActionListener(e -> handleSend());

        client.setConsumer(this::displayMessage); // Connect consumer to displayMessage

        frame.setVisible(true);
    }

    /** 
     * Helper method to handle sending messages using the GUI
     */
    private void handleSend() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        if ("exit".equalsIgnoreCase(message)) {
            client.log("[System] Exiting chat...");
            frame.dispose();
            client.stop();
            return;
        }

        try {
            client.sendMessage(message);
            client.log("You: " + message);
            inputField.setText("");
        } catch (Exception e) {
            client.log("[Error] " + e.getMessage());
        }
    }

    /**
     * Helper method to display messages in the chatArea
     */
    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }
}
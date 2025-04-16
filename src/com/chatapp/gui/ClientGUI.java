package com.chatapp.gui;

/*
 * First draft GUI
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import com.chatapp.cryptography.*;
import com.chatapp.client.*;

public class ClientGUI extends JFrame {

    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    // Initialize GUI
    public ClientGUI() {
        setTitle("Chat Client");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    // GUI Initialization details
    private void initComponents() {
        hostField = new JTextField("localhost");
        portField = new JTextField("12345");
        connectButton = new JButton("Connect");

        connectButton.addActionListener(this::onConnect);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Host:"));
        panel.add(hostField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel());
        panel.add(connectButton);

        add(panel);
    }

    private void onConnect(ActionEvent e) {
        String host = hostField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MessageEncryptor encryptor = new DummyEncryptor(); // Use your real encryptor if available
        Client client = new Client(host, port, encryptor);

        // Run the client in a new thread so it doesn't block the GUI
        new Thread(client::start).start();

        // Optional: disable connect button
        connectButton.setEnabled(false);
    }
}

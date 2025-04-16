package com.chatapp;

import com.chatapp.gui.ClientGUI;

public class ChatAppLauncher {
    public static void main(String[] args) {
        // Start the GUI on the Event Dispatch Thread (best practice for Swing)
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}

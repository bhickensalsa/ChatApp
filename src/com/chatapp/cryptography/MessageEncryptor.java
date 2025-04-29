package com.chatapp.cryptography;

import java.security.PublicKey;

/**
 * Interface for message encryption and decryption.
 * 
 * This interface defines two methods for encrypting and decrypting messages.
 * Implementations of this interface will provide the actual encryption and decryption logic
 * to ensure that the messages can be securely transmitted over a network or other communication medium.
 * 
 * @author Philip Jonsson
 * @author Mohamed El Yahioui
 * @version 2025-04-25
 */
public interface MessageEncryptor {

    /**
     * Encrypts the given message.
     */
    String encrypt(String message, PublicKey receiverPublicKey) throws Exception;

    /**
     * Decrypts the given message.
     */
    String decrypt(String encryptedMessage) throws Exception;

    /**
     * Returns the public key of the encryptor.
     */
    PublicKey getPublicKey();
}
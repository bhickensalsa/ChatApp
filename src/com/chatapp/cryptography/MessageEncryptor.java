package com.chatapp.cryptography;

/**
 * Interface for message encryption and decryption.
 * 
 * This interface defines two methods for encrypting and decrypting messages.
 * Implementations of this interface will provide the actual encryption and decryption logic
 * to ensure that the messages can be securely transmitted over a network or other communication medium.
 * 
 * @author Philip Jonsson
 * @version 2025-04-14
 */
public interface MessageEncryptor {

    /**
     * Encrypts the given message.
     */
    String encrypt(String message) throws Exception;

    /**
     * Decrypts the given message.
     */
    String decrypt(String message) throws Exception;
}
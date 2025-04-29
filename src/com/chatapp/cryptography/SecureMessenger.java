package com.chatapp.cryptography;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * The SecureMessenger class provides methods for secure communication using RSA encryption.
 * It automatically generates a 2048-bit RSA key pair upon creation.
 * 
 * Users can:
 * - Encrypt messages with another user's public key
 * - Decrypt messages with their own private key
 * - Access their own public/private keys for sharing or storage
 * 
 * @author Mohamed El Yahioui
 * @version 1.0
 */
public class SecureMessenger implements MessageEncryptor{
    private PublicKey publicKey;
    private PrivateKey privateKey;
    
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    /**
     * Constructs a SecureMessenger instance and generates a new RSA key pair.
     * @throws Exception if key generation fails.
     */
    public SecureMessenger() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }
    
    /**
     * Encrypts a plaintext message using the recipient's public key.
     * 
     * @param message the message to encrypt
     * @param receiverPublicKey the public key of the message recipient
     * @return the encrypted message as a Base64 string
     * @throws Exception if encryption fails
     */
    public String encrypt(String message, PublicKey receiverPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);
        byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

     /**
     * Decrypts an encrypted message using the user's private key.
     * 
     * @param encryptedMessage the encrypted message in Base64 format
     * @return the original plaintext message
     * @throws Exception if decryption fails
     */
    public String decrypt(String encryptedMessage) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decrypted, "UTF-8");
    }
}
/**
 * A simple implementation of the MessageEncryptor interface for testing purposes.
 * 
 * This class provides basic encryption and decryption methods that simply prepend
 * and remove the string "ENCRYPTED: " to/from the message. This is a placeholder
 * implementation for testing and demonstration purposes. It does not offer actual security.
 * 
 * This class is intended to be replaced with a real encryption implementation in a production environment.
 * 
 * @author Philip Jonsson
 * @version 2025-04-14
 */
public class DummyEncryptor implements MessageEncryptor {

    /**
     * Encrypts the given message by adding "ENCRYPTED: " to it.
     */
    @Override
    public String encrypt(String message) {
        return "ENCRYPTED: " + message;
    }

    /**
     * Decrypts the given message by removing the "ENCRYPTED: " prefix.
     */
    @Override
    public String decrypt(String message) {
        return message.replace("ENCRYPTED: ", "");
    }
}
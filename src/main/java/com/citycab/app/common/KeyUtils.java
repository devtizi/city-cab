package com.citycab.app.common;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class KeyUtils {

    private KeyUtils() {}

    // Common key markers for different PEM formats
    private static final String[] PRIVATE_KEY_MARKERS = {
        "-----BEGIN PRIVATE KEY-----",
        "-----END PRIVATE KEY-----",
        "-----BEGIN RSA PRIVATE KEY-----", 
        "-----END RSA PRIVATE KEY-----",
        "-----BEGIN EC PRIVATE KEY-----",
        "-----END EC PRIVATE KEY-----"
    };

    private static final String[] PUBLIC_KEY_MARKERS = {
        "-----BEGIN PUBLIC KEY-----",
        "-----END PUBLIC KEY-----",
        "-----BEGIN RSA PUBLIC KEY-----",
        "-----END RSA PUBLIC KEY-----"
    };

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static final String RSA_ALGORITHM = "RSA";
    //private static final String EC_ALGORITHM = "EC";

    /**
     * Loads a private key from a PEM file in classpath resources
     */
    public static PrivateKey loadPrivateKey(final String pemPath) throws Exception {
        return loadPrivateKey(pemPath, RSA_ALGORITHM);
    }

    /**
     * Loads a private key from a PEM file with specified algorithm
     */
    public static PrivateKey loadPrivateKey(final String pemPath, final String algorithm) throws Exception {
        validatePath(pemPath);
        final String keyContent = readKeyFromResource(pemPath);
        final String cleanKey = cleanPemKey(keyContent, PRIVATE_KEY_MARKERS);
        final byte[] decoded = Base64.getDecoder().decode(cleanKey);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
    }

    /**
     * Loads a private key from an absolute file path
     */
    public static RSAPrivateKey loadPrivateKeyFromFile(final String filePath) throws Exception {
        return loadPrivateKeyFromFile(filePath, RSA_ALGORITHM);
    }

    /**
     * Loads a private key from an absolute file path with specified algorithm
     */
    public static RSAPrivateKey loadPrivateKeyFromFile(final String filePath, final String algorithm) throws Exception {
        validatePath(filePath);
        final String keyContent = readKeyFromFile(filePath);
        final String cleanKey = cleanPemKey(keyContent, PRIVATE_KEY_MARKERS);
        final byte[] decoded = Base64.getDecoder().decode(cleanKey);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return  (RSAPrivateKey) KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
    }

    /**
     * Loads a public key from a PEM file in classpath resources
     */
    public static PublicKey loadPublicKey(final String pemPath) throws Exception {
        return loadPublicKey(pemPath, RSA_ALGORITHM);
    }

    /**
     * Loads a public key from a PEM file with specified algorithm
     */
    public static PublicKey loadPublicKey(final String pemPath, final String algorithm) throws Exception {
        validatePath(pemPath);
        final String keyContent = readKeyFromResource(pemPath);
        final String cleanKey = cleanPemKey(keyContent, PUBLIC_KEY_MARKERS);
        final byte[] decoded = Base64.getDecoder().decode(cleanKey);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance(algorithm).generatePublic(keySpec);
    }

    /**
     * Loads a public key from an absolute file path
     */
    public static RSAPublicKey loadPublicKeyFromFile(final String filePath) throws Exception {
        return loadPublicKeyFromFile(filePath, RSA_ALGORITHM);
    }

    /**
     * Loads a public key from an absolute file path with specified algorithm
     */
    public static RSAPublicKey loadPublicKeyFromFile(final String filePath, final String algorithm) throws Exception {
        validatePath(filePath);
        final String keyContent = readKeyFromFile(filePath);
        final String cleanKey = cleanPemKey(keyContent, PUBLIC_KEY_MARKERS);
        final byte[] decoded = Base64.getDecoder().decode(cleanKey);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance(algorithm).generatePublic(keySpec);
    }

    /**
     * Reads key content from classpath resources
     */
    private static String readKeyFromResource(final String path) throws Exception {
        try (final InputStream is = KeyUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Key not found in classpath: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Reads key content from file system
     */
    private static String readKeyFromFile(final String path) throws Exception {
        final byte[] bytes = Files.readAllBytes(Paths.get(path));
        if (bytes.length == 0) {
            throw new IllegalArgumentException("Key file is empty: " + path);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Cleans PEM key by removing markers and whitespace
     */
    private static String cleanPemKey(String key, String[] markers) {
        for (String marker : markers) {
            key = key.replace(marker, "");
        }
        return WHITESPACE_PATTERN.matcher(key).replaceAll("");
    }

    /**
     * Validates that the path is not null or empty
     */
    private static void validatePath(final String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Key path cannot be null or empty");
        }
    }

    /**
     * Utility method to detect key type from content
     */
    public static String detectKeyType(final String keyContent) {
        if (keyContent.contains("BEGIN PRIVATE KEY") || keyContent.contains("BEGIN RSA PRIVATE KEY")) {
            return "PRIVATE";
        } else if (keyContent.contains("BEGIN PUBLIC KEY") || keyContent.contains("BEGIN RSA PUBLIC KEY")) {
            return "PUBLIC";
        } else if (keyContent.contains("BEGIN EC PRIVATE KEY")) {
            return "EC_PRIVATE";
        } else {
            return "UNKNOWN";
        }
    }
}
package com.sap.sailing.server.gateway.serialization.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;

public class HashedStringUtil {
    public static String toHashedString(String stringToHash) {
        assert stringToHash != null;
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        final byte[] digest = messageDigest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        final StringJoiner joiner = new StringJoiner(" ");
        for (byte b : digest) {
            joiner.add(String.format("%02X", b));
        }
        return joiner.toString();
    }
}

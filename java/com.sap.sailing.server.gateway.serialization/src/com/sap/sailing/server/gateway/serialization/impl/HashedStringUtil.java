package com.sap.sailing.server.gateway.serialization.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Stream.Builder<Byte> builder = Stream.builder();
        for (int i = 0; i < digest.length; i++) {
            builder.add(digest[i]);
        }
        return builder.build()
            .map(i -> String.format("%02X", i))
            .collect(Collectors.joining(" "));
    }
}

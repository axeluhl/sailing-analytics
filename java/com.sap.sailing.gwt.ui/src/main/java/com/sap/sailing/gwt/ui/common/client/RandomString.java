package com.sap.sailing.gwt.ui.common.client;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomString {

    /**
     * Generate a random string containing uppercase letters and numbers.
     * 
     * @param length
     *            length of the string to generate
     * @return random string
     */
    public static String createRandomSecret(int length) {
        String randomString = Stream.generate(Math::random).map(r -> (int) (r * 100))
                .filter(i -> (i > 47 && i < 58 || i > 64 && i < 90)).limit(length)
                .map(i -> String.valueOf((char) i.intValue())).collect(Collectors.joining());
        return randomString;
    }

}

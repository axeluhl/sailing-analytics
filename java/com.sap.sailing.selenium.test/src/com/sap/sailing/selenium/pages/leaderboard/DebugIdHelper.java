package com.sap.sailing.selenium.pages.leaderboard;

/**
 * <p>Helper class for generating debug identifiers.</p>
 */
public class DebugIdHelper {
    /**
     * <p>Creates a debug identifier for the given enum value. The identifier is the camel cased name of the enum value
     *   as declared in its enum declaration. The name is split into single words using the underscore.</p>
     * 
     * @param value
     *   The enum value for which the identfier should be created.
     * @return
     *   A debug identifier for the given enum value.
     */
    public static <T extends Enum<?>> String createDebugId(T value) {
        return createDebugId(value.name(), "_");
    }
    
    /**
     * <p>Creates a debug identifier for the given name. The name is splited into single words using all whitespace
     *   characters.</p>
     * 
     * @param value
     *   The name for which the identifier should be created.
     * @return
     *   A debug identifier for the given enum value.
     */
    public static String createDebugId(String name) {
        return createDebugId(name, "\\s");
    }
    
    private static String createDebugId(String name, String splitRegex) {
        StringBuilder builder = new StringBuilder();
        
        for (String token : name.split(splitRegex)) {
            builder.append(token.substring(0, 1).toUpperCase());
            builder.append(token.substring(1, token.length()).toLowerCase());
        }
        return builder.toString();
    }
}

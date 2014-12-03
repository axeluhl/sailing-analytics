package com.sap.sailing.gwt.common.client;

import com.google.gwt.dom.client.Style;

public class CSS3Util {
     public static void setProperty(Style style, String baseCamelCasePropertyName, String value) {
        for (String browserTypePrefix : getBrowserTypePrefixes()) {
            style.setProperty(getBrowserSpecificPropertyName(browserTypePrefix, baseCamelCasePropertyName), value);
        }
    }

     public static void setDashedProperty(Style style, String baseCamelCasePropertyName, String value) {
         for (String browserTypePrefix : getBrowserTypePrefixes()) {
             style.setProperty(getBrowserSpecificDashedPropertyName(browserTypePrefix, baseCamelCasePropertyName), value);
         }
     }

    /**
     * @return the prefixes required for new CSS3-style elements such as "transition" or "@keyframe", including the
     *         empty string, "moz" and "webkit" as well as others
     */
    private static String[] getBrowserTypePrefixes() {
        return new String[] { "", /* Firefox */ "moz", /* IE */ "ms", /* Opera */ "o", /* Chrome and Mobile */ "webkit" };
    }
    
    /**
     * Converts something like "transformOrigin" to "-<code>browserType</code>-transform-origin"
     * 
     * @param browserType
     *            a browser type string as received from {@link #getBrowserTypePrefixes()}. If empty or null, the
     *            original property name is returned unchanged
     * @param camelCaseString
     *            the original camel-cased property name, such as "transformOrigin"
     */
    public static String getBrowserSpecificDashedPropertyName(String browserType, String camelCaseString) {
        StringBuilder result = new StringBuilder();
        if (browserType != null && !browserType.isEmpty()) {
            result.append('-');
            result.append(browserType);
            result.append('-');
        }
        for (int i=0; i<camelCaseString.length(); i++) {
            final char c = camelCaseString.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('-');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    public static String getBrowserSpecificPropertyName(String browserType, String basePropertyName) {
        final String result;
        if (browserType == null || browserType.isEmpty()) {
                result = basePropertyName;
        } else {
                result = browserType+basePropertyName.substring(0, 1).toUpperCase()+basePropertyName.substring(1);
        }
        return result;
    }
}

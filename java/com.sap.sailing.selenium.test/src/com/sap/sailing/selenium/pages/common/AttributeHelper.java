package com.sap.sailing.selenium.pages.common;

import org.openqa.selenium.WebElement;

/**
 * Simple attribute helper.
 */
public class AttributeHelper {
    /**
     * Checks if the given attribute name is "enabled". This convenience method accepts "true" and "1" as values for
     * "enabled", all other values are considered as "disabled".
     * 
     * @param element
     * @param attributeName
     * @return
     */
    public static boolean isEnabled(WebElement element, String attributeName) {
        String attributeValue = element.getAttribute(attributeName);
        return "true".equalsIgnoreCase(attributeValue) || "1".equalsIgnoreCase(attributeValue);
    }
}

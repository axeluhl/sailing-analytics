package com.sap.sailing.selenium.pages.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.WebElement;

public class CSSHelper {
    /**
     * <p>Creates an XPath 1.0 predicate that matches if an element has the given style sheet class in its "class"
     *   attribute.</p>
     * 
     * @param className
     *   The name of the style sheet class to check for.
     * @return
     *   An XPath predicate that matches if an element has the given style sheet class in its "class" attribute.
     */
    public static String containsCSSClassPredicate(String className) {
        return "contains(concat(' ',normalize-space(@class),' '),' " + className + " ')";
    }
    
    public static String containsCSSClassesPredicate(String... classNames) {
        StringBuilder builder = new StringBuilder(70 * classNames.length);
        for(String className : classNames) {
            builder.append(containsCSSClassPredicate(className));
            builder.append(" and ");
        }
        return builder.substring(0, builder.length() - 5);
    }
    
    public static List<String> getCSSClassNames(WebElement element) {
        String cssClasses = element.getAttribute(CSSConstants.CSS_CLASS_ATTRIBUTE_NAME);
        final List<String> result;
        if (cssClasses == null) {
            result = Collections.emptyList();
        } else {
            ArrayList<String> cssClassNames = new ArrayList<>();
            for (String part : cssClasses.split(" ")) {
                String cssClass = part.trim();
                if (!cssClass.isEmpty()) {
                    cssClassNames.add(cssClass);
                }
            }
            result = cssClassNames;
        }
        return result;
    }
    
    public static boolean hasCSSClass(WebElement element, String cssClass) {
        if (cssClass == null || cssClass.isEmpty()) {
            return true;
        }
        List<String> cssClasses = getCSSClassNames(element);
        return cssClasses.contains(cssClass);
    }
    
    private CSSHelper() {
        // Do not instantiate a utility class
    }
}

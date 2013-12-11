package com.sap.sailing.selenium.pages.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.WebElement;

public class CSSHelper {    
    public static List<String> getCSSClassNames(WebElement element) {
        String cssClasses = element.getAttribute(CSSConstants.CSS_CLASS_ATTRIBUTE_NAME);
        
        if(cssClasses == null)
            return Collections.emptyList();
        
        ArrayList<String> cssClassNames = new ArrayList<>();
        
        for(String part : cssClasses.split(" ")) {
            String cssClass = part.trim();
            
            if(!cssClass.isEmpty())
                cssClassNames.add(cssClass);
        }
        
        return cssClassNames;
    }
    
    public static boolean hasCSSClass(WebElement element, String cssClass) {
        if(cssClass == null || cssClass.isEmpty())
            return true;
        
        List<String> cssClasses = getCSSClassNames(element);
        
        return cssClasses.contains(cssClass);
    }
    
    private CSSHelper() {
        // Do not instantiate a utility class
    }
}

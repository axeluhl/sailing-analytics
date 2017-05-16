package com.sap.sailing.selenium.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openqa.selenium.By;

/**
 * <p>Used to mark a field on a page object to indicate an mechanism for locating the element or a list of elements.
 *   In conjunction with a page factory this allows users to quickly and easily create page objects.</p>
 *
 * <p>To use this annotation you have to specifying the locating mechanism ("how") as well as an appropriate value to
 *   use ("using") on a field of type {@code WebElement} or {@code List<WebElement>}. This will delegate down
 *   to the matching methods in the defined {@code By} class.</p>
 * 
 * <p>An usage could be look as follows:
 * 
 * <pre>
 *   &#64;FindBy(how = ById.class, using = "myImage")
 *   WebElement image;
 *   
 *   &#64;FindBy(how = ByTagName.class, using = "href")
 *   List<WebElement> links;
 * </pre></p>
 * 
 * @author
 *   D049941
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindBy {
    /**
     * <p>Returns the mechanism to use for locating the element or the list of elements.</p>
     * 
     * @return
     *   The mechanism to use for locating the element or the list of elements.
     */
    Class<? extends By> how();

    /**
     * <p>Returns the value to use for locating the element or the list of elements.</p>
     * 
     * @return
     *   The value to use for locating the element or the list of elements.
     */
    String using();
}

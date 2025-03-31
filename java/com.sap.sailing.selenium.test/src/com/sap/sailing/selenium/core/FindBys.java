package com.sap.sailing.selenium.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used to mark a field on a page object to indicate that the lookup should use a series of mechanism for locating
 *   the element or a list of elements, whereby each of the locators in sequence has to match.</p>
 * 
 * <p>To find all elements that representing a link which appear under an element with the stylesheet class "menu", a 
 *   usage could look as follows:
 * 
 * <pre>
 *   &#64;FindBys({&#64;FindBy(how = ByClassName.class, using = "menu"),
 *       &#64;FindBy(how = ByTagName.class, using = "href")
 *   })
 *   List<WebElement> menuEntries;
 * </pre></p>
 * 
 * @author
 *   D049941
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindBys {
    /**
     * <p>Returns the series of mechanisms to use for locating the element or the list of elements.</p>
     * 
     * @return
     *   The series of mechanisms to use for locating the element or the list of elements.
     */
    FindBy[] value();
}

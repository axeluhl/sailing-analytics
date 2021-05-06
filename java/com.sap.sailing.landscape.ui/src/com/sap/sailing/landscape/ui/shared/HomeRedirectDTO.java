package com.sap.sailing.landscape.ui.shared;

/**
 * Redirects users to the {@code "/gwt/Home.html"} entry point. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class HomeRedirectDTO implements RedirectDTO {
    @Override
    public String getPath() {
        return "/gwt/Home.html";
    }
}

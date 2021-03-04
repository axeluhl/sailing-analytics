package com.sap.sailing.landscape.ui.shared;

/**
 * Represents a redirect to the {@code "/index.html"} landing page. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class PlainRedirectDTO implements RedirectDTO {
    @Override
    public String getPath() {
        return "/index.html";
    }
}

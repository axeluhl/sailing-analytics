package com.sap.sailing.landscape.ui.shared;

/**
 * Redirects users to the {@code "/gwt/Home.html"} entry point. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class HomeRedirectDTO extends AbstractRedirectDTO {
    public HomeRedirectDTO() {
        this(Type.HOME);
    }
    
    public HomeRedirectDTO(Type type) {
        super(type);
    }
    
    @Override
    public String getPath() {
        return "/gwt/Home.html";
    }
    
    static HomeRedirectDTO parse(String redirectPath) {
        final HomeRedirectDTO candidate = new HomeRedirectDTO();
        final HomeRedirectDTO result;
        if (candidate.getPathAndQuery().equals(redirectPath)) {
            result = candidate;
        } else {
            result = null;
        }
        return result;
    }
}

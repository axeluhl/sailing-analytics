package com.sap.sse.landscape.aws;

/**
 * Redirects users to the {@code "/gwt/Home.html"} entry point. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class HomeRedirectDTO extends AbstractRedirectDTO {
    private static final long serialVersionUID = 3487537611111235833L;

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
    
    @Override
    public void accept(RedirectVisitor visitor) throws Exception {
        visitor.visit(this);
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

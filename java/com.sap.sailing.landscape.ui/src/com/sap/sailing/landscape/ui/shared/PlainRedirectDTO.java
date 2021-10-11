package com.sap.sailing.landscape.ui.shared;

/**
 * Represents a redirect to the {@code "/index.html"} landing page. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class PlainRedirectDTO extends AbstractRedirectDTO {
    public PlainRedirectDTO() {
        super(RedirectDTO.Type.PLAIN);
    }
    
    @Override
    public String getPath() {
        return "/index.html";
    }
    
    @Override
    public void accept(RedirectVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    static PlainRedirectDTO parse(String redirectPath) {
        final PlainRedirectDTO candidate = new PlainRedirectDTO();
        final PlainRedirectDTO result;
        if (RedirectDTO.toString(candidate.getPath(), candidate.getQuery()).equals(redirectPath)) {
            result = candidate;
        } else {
            result = null;
        }
        return result;
    }
}

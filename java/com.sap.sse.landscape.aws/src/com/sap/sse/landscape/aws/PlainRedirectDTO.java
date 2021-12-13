package com.sap.sse.landscape.aws;

/**
 * Represents a redirect to the {@code "/index.html"} landing page. Not parameterized further.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class PlainRedirectDTO extends AbstractRedirectDTO {
    private static final long serialVersionUID = -921945852175238474L;

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

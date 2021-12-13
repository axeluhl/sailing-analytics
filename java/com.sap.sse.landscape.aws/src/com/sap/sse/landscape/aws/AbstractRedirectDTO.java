package com.sap.sse.landscape.aws;

public abstract class AbstractRedirectDTO implements RedirectDTO {
    private static final long serialVersionUID = -7412244909720631505L;
    private RedirectDTO.Type type;
    @Deprecated
    AbstractRedirectDTO() {} // for GWT serialization only
    
    AbstractRedirectDTO(RedirectDTO.Type type) {
        if (type == null) {
            throw new NullPointerException("RedirectDTO's type must not be null");
        }
        this.type = type;
    }
    
    @Override
    public RedirectDTO.Type getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return getPathAndQuery();
    }
}

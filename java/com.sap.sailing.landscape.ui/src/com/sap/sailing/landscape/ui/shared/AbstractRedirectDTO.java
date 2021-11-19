package com.sap.sailing.landscape.ui.shared;

public abstract class AbstractRedirectDTO implements RedirectDTO {
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

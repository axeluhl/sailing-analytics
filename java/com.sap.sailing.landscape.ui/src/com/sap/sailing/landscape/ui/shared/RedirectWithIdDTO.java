package com.sap.sailing.landscape.ui.shared;

import java.util.UUID;

/**
 * A redirect that is parameterized by a {@link UUID}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class RedirectWithIdDTO extends HomeRedirectDTO {
    private UUID id;
    
    @Deprecated
    RedirectWithIdDTO() {} // for GWT RPC only
    
    public RedirectWithIdDTO(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}

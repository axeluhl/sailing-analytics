/**
 * 
 */
package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Alexander Ries
 * 
 */
public interface RibDashboardImageResources extends ClientBundle {

    public static final RibDashboardImageResources INSTANCE = GWT.create(RibDashboardImageResources.class);

    @Source("images/logo_sap.png")
    ImageResource logo_sap();

    @Source("images/logoess.png")
    ImageResource logoess();

    @Source("images/leftarrow.png")
    ImageResource left();

    @Source("images/rightarrow.png")
    ImageResource right();
    
    @Source("images/leftarrow_disabled.png")
    ImageResource leftdisabled();

    @Source("images/rightarrow_disabled.png")
    ImageResource rightdisabled();
    
    @Source("images/windarrow.png")
    ImageResource windarrow();
    
    @Source("images/compass.png")
    ImageResource compass();
    
    @Source("images/watch.png")
    ImageResource watch();
    
    @Source("images/settings.png")
    ImageResource settings();
}

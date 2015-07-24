package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.Component;

public interface DataMiningComponentProvider extends Component<AbstractSettings> {

    /**
     * Tells the provider, that {@link #reloadComponents()} will be called in the future.
     */
    public void awaitReloadComponents();
    
    /**
     * @return <code>true</code>, if the provider is reloading its components
     *         or is waiting for the {@link #reloadComponents()} call.
     */
    public boolean isAwatingReload();
    
    public void reloadComponents();
    
}

package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.sap.sailing.gwt.ui.client.shared.components.Component;

/**
 * A perspective is a composition of UI components into a view/page
 * @author c5163874
 *
 */
public interface Perspective {
    Iterable<Component<?>> getComponents();
    
    String getPerspectiveName();
}

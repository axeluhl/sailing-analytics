package com.sap.sailing.gwt.ui.client.shared.racemap;

/**
 * Used to get the information that appears on hovering over a line and that is displayed in
 * {@link #adjustInfoOverlayForVisibleLine}
 */
interface LineInfoProvider {
    String getLineInfo();
    default boolean isShowInfoOverlayWithHelplines() {
        return true;
    };
}
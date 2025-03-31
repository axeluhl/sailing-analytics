package com.sap.sailing.gwt.ui.client.shared.racemap;

@FunctionalInterface
public interface ColorlineColorProvider {
    /**
     * @param fixIndexInTail
     *            zero-based index into the visible tail; 0 means the first visible fix
     */
    public String getColor(int fixIndexInTail);
}

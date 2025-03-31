package com.sap.sailing.gwt.home.shared.partials.launchpad;

import com.google.gwt.dom.client.Element;

/**
 * Interface for a launch pad controller class for a specific type of data.
 * 
 * @param <D>
 *            the actual data type
 */
public interface LaunchPadController<D> {

    /**
     * Provides the direct link URL for the given data.
     * 
     * @param data
     *            the data to get the link URL for
     * @return the direct link URL (as {@link String})
     */
    String getDirectLinkUrl(D data);

    /**
     * Shows a launch pad for the given data relative to the provided element.
     * 
     * @param data
     *            the data to create a launch pad for
     * @param relTo
     *            the {@link Element element} to show the launch pad relative to
     */
    void showLaunchPad(D data, Element relTo);

    /**
     * Determine whether a direct link should be/is rendered for the given data instead of a menu popup.
     * 
     * @param data
     *            the data which should be/is rendered
     * @return <code>true</code> if a direct link should be/is rendered, <code>false</code> otherwise
     */
    boolean renderAsDirectLink(D data);

}

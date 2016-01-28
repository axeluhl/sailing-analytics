package com.sap.sailing.gwt.ui.shared;

import com.github.gwtbootstrap.client.ui.resources.ResourceInjector;
import com.github.gwtbootstrap.datetimepicker.client.ui.DateTimeBox;
import com.github.gwtbootstrap.datetimepicker.client.ui.resources.DatetimepickerResourceInjector;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;

public class BetterDateTimeBox extends DateTimeBox {
    
    /**
     * Initializes all needed JavaScript and CSS. Should be called once per {@link EntryPoint}.
     */
    public static void initialize() {
        // We don't want the full bootstrap css...
        ResourceInjector.injectResourceCssAsFile("font-awesome.min.css");
        ResourceInjector.configure();
        DatetimepickerResourceInjector.configureWithCssFile();
    }
    
    public BetterDateTimeBox() {
        super.setFormat("mm/dd/yyyy hh:ii");
    }
    
    public Element getPicker() {
        return getNativePickerElement(getBox().getElement());
    }
    
    private native Element getNativePickerElement(Element boxElement) /*-{
        return $wnd.jQuery(boxElement).data('datetimepicker').picker[0];
    }-*/;

    @Override
    public com.github.gwtbootstrap.client.ui.TextBox getBox() {
        return super.getBox();
    }
}
